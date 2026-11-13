---
name: export-key
overview: Add the Thales A8/A9 Export a Key command as a new gRPC RPC, supporting only the base export fields without Atalla variant.
todos:
  - id: proto
    content: Add ExportKey RPC and messages to hsm.proto
    status: completed
  - id: regenerate
    content: Regenerate protobuf sources with mvn compile
    status: completed
  - id: command
    content: Create ExportKeyA8Command with A8/A9 packing and unpacking
    status: completed
  - id: grpc
    content: Add exportKey method to HsmGrpcService
    status: completed
  - id: tests
    content: Write unit tests for ExportKeyA8Command
    status: completed
  - id: verify
    content: Run mvn test and fix any regressions
    status: completed
isProject: false
---

# Plan: Add A8/A9 Export a Key command

Add the Thales A8/A9 Export a Key command as a new gRPC RPC, supporting only the base export fields and excluding the Atalla variant.

## Scope
- Support the **base A8 fields**:
  - `Key Type` (3 hex chars, e.g. `001` for ZPK)
  - `ZMK/TMK Flag` (1 char, `0` = ZMK, `1` = TMK)
  - `ZMK (or TMK)` (combined scheme + value string, e.g. `UE685...`)
  - `Key` (the key encrypted under the LMK, combined scheme + value string, e.g. `U5F2D...`)
  - `Key Scheme (ZMK or TMK)` (1 char output scheme, e.g. `U`)
- Out of scope for this iteration: Atalla variant, `%` LMK identifier, TR-31/Thales key block sections (`&` / `!` delimiters), and message trailer.
- ZMK/TMK and Key values are passed as **combined scheme + value strings**, matching the A8 field definitions and the existing A6 Import Key convention.

## Proposed Changes

1. **Extend `src/main/proto/hsm.proto`**
   - Add `rpc ExportKey(ExportKeyRequest) returns (ExportKeyResponse);` to `HsmService`.
   - Add `message ExportKeyRequest` with fields:
     - `string key_type = 1;`
     - `string zmk_tmk_flag = 2;`
     - `string zmk_tmk = 3;`
     - `string key = 4;`
     - `string key_scheme_zmk_tmk = 5;`
   - Add `message ExportKeyResponse` with fields:
     - `string error_code = 1;`
     - `string key_under_zmk_tmk = 2;`
     - `string key_check_value = 3;`

2. **Regenerate protobuf sources**
   - Run `mvn compile` so the new `ExportKeyRequest`, `ExportKeyResponse`, and service stubs are generated.

3. **Create `src/main/java/com/company/cvhsm/hsm/command/ExportKeyA8Command.java`**
   - Implement `HsmCommand<ExportKeyRequest, ExportKeyResponse>`.
   - Return command code `A8` and response code `A9`.
   - `packRequestBody`:
     - Validate `key_type` is exactly 3 chars, `zmk_tmk_flag` is `0` or `1`, `zmk_tmk` and `key` are non-empty, and `key_scheme_zmk_tmk` is exactly 1 char.
     - Write the fields in order as ASCII bytes: `key_type + ';' + zmk_tmk_flag + zmk_tmk + key + key_scheme_zmk_tmk`.
   - `unpackResponseBody`:
     - If the body is empty, return an `ExportKeyResponse` with only the error code.
     - Convert the body to ASCII and take the last 6 characters as the `key_check_value`.
     - Treat the remaining leading characters as `key_under_zmk_tmk` (which includes the scheme prefix returned by the HSM).

4. **Expose the new RPC in `src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java`**
   - Add `exportKey(ExportKeyRequest, StreamObserver<ExportKeyResponse>)` that resolves the `A8` command via `HsmCommandFactory` and invokes it through `HsmCommandInvoker`, mirroring the existing `generateKey` and `importKey` methods.

5. **Write `src/test/java/com/company/cvhsm/hsm/command/ExportKeyA8CommandTest.java`**
   - Test that command/response codes are `A8`/`A9`.
   - Test packing a base A8 request body for ZPK export (`key_type=001`, `zmk_tmk_flag=0`, `zmk_tmk=UE685...`, `key=U5F2D...`, `key_scheme_zmk_tmk=U`).
   - Test validation rejects invalid flag, missing values, and wrong field lengths.
   - Test unpacking a successful A9 response into `key_under_zmk_tmk` and `key_check_value`.
   - Test that an empty response body returns only the error code.

6. **Verify the build**
   - Run `mvn test` to ensure the new command, gRPC service, and all existing tests pass.

## Files to Change
- `[src/main/proto/hsm.proto](src/main/proto/hsm.proto)`
- `[src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java](src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java)`
- `[src/main/java/com/company/cvhsm/hsm/command/ExportKeyA8Command.java](src/main/java/com/company/cvhsm/hsm/command/ExportKeyA8Command.java)` (new)
- `[src/test/java/com/company/cvhsm/hsm/command/ExportKeyA8CommandTest.java](src/test/java/com/company/cvhsm/hsm/command/ExportKeyA8CommandTest.java)` (new)

## Out of Scope
- Atalla variant.
- LMK identifier (`%` delimiter).
- TR-31 / Thales key block optional sections (`&` and `!` delimiters).
- End-message delimiter and message trailer.
- Refactoring the existing A0 Generate Key or A6 Import Key commands; the A8 command will use its own self-contained validation/parsing logic.