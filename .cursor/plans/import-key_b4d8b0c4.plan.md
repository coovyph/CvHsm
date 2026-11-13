---
name: import-key
overview: Add the Thales A6/A7 Import a Key command as a new gRPC RPC using the existing HsmCommand framework, supporting only the base fields.
todos:
  - id: proto
    content: Add ImportKey RPC and messages to hsm.proto
    status: completed
  - id: regenerate
    content: Regenerate protobuf sources with mvn compile
    status: completed
  - id: command
    content: Create ImportKeyA6Command with A6/A7 packing and unpacking
    status: completed
  - id: grpc
    content: Add importKey method to HsmGrpcService
    status: completed
  - id: tests
    content: Write unit tests for ImportKeyA6Command
    status: completed
  - id: verify
    content: Run mvn test and fix any regressions
    status: completed
isProject: false
---

# Plan: Add A6/A7 Import a Key command

I reviewed the A6 "Import a Key" command requirements (page A6 of the payShield 10K manual and the `.cursor/requirements/import_key.md` file). The goal is to expose the Thales A6/A7 Import Key command through the existing gRPC gateway, using the same `HsmCommand` framework as the A0 Generate Key command.

## Scope
- Support the **base A6 fields only**:
  - `Key Type` (3 hex chars, e.g. `001` for ZPK)
  - `ZMK` (combined string with scheme prefix, e.g. `UE685...`)
  - `Key (ZMK)` (combined string with scheme prefix, e.g. `XAC4D...`)
  - `Key Scheme (LMK)` (1 char, e.g. `U`)
- Out of scope for this iteration: Atalla variant, LMK identifier (`%`), TR-31 keyblock sections (`#`), and message trailer.
- ZMK and key-under-ZMK values are passed as **combined scheme + value strings**, matching the A6 field definitions.

## Proposed Changes

1. **Extend `src/main/proto/hsm.proto`**
   - Add `rpc ImportKey(ImportKeyRequest) returns (ImportKeyResponse);` to `HsmService`.
   - Add `message ImportKeyRequest` with fields:
     - `string key_type = 1;`
     - `string zmk = 2;`
     - `string key_under_zmk = 3;`
     - `string key_scheme_lmk = 4;`
   - Add `message ImportKeyResponse` with fields:
     - `string error_code = 1;`
     - `string key_under_lmk = 2;`
     - `string key_check_value = 3;`

2. **Regenerate protobuf sources**
   - Run `mvn compile` so the new `ImportKeyRequest`, `ImportKeyResponse`, and service stubs are generated in `target/generated-sources/protobuf`.

3. **Create `src/main/java/com/company/cvhsm/hsm/command/ImportKeyA6Command.java`**
   - Implement `HsmCommand<ImportKeyRequest, ImportKeyResponse>`.
   - Return command code `A6` and response code `A7`.
   - `packRequestBody`:
     - Validate `key_type` is exactly 3 chars, `key_scheme_lmk` is exactly 1 char, and `zmk`/`key_under_zmk` are non-empty.
     - Write the fields in order as ASCII bytes: `key_type + zmk + key_under_zmk + key_scheme_lmk`.
   - `unpackResponseBody`:
     - If the body is empty, return an `ImportKeyResponse` with only the error code.
     - Convert the body to ASCII and take the last 6 characters as the `key_check_value`.
     - Treat the remaining leading characters as `key_under_lmk` (which includes the LMK scheme prefix returned by the HSM).

4. **Expose the new RPC in `src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java`**
   - Add `importKey(ImportKeyRequest, StreamObserver<ImportKeyResponse>)` that resolves the `A6` command via `HsmCommandFactory` and invokes it through `HsmCommandInvoker`, mirroring the existing `generateKey` method.

5. **Write `src/test/java/com/company/cvhsm/hsm/command/ImportKeyA6CommandTest.java`**
   - Test that command/response codes are `A6`/`A7`.
   - Test packing a base A6 request body for a ZPK import (`key_type=001`, `zmk=UE685...`, `key_under_zmk=XAC4D...`, `key_scheme_lmk=U`).
   - Test validation rejects missing or malformed fields.
   - Test unpacking a successful A7 response into `key_under_lmk` and `key_check_value`.
   - Test that an empty response body returns only the error code.

6. **Verify the build**
   - Run `mvn test` to ensure the new command, gRPC service, and all existing tests pass.

## Files to Change
- `[src/main/proto/hsm.proto](src/main/proto/hsm.proto)`
- `[src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java](src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java)`
- `[src/main/java/com/company/cvhsm/hsm/command/ImportKeyA6Command.java](src/main/java/com/company/cvhsm/hsm/command/ImportKeyA6Command.java)` (new)
- `[src/test/java/com/company/cvhsm/hsm/command/ImportKeyA6CommandTest.java](src/test/java/com/company/cvhsm/hsm/command/ImportKeyA6CommandTest.java)` (new)

## Out of Scope
- Atalla variant, `%` LMK identifier, and `#` keyblock/TR-31 optional sections.
- End-message delimiter and message trailer handling.
- Refactoring the existing A0 Generate Key command; the A6 command will use its own self-contained validation/parsing logic.