---
name: translate-pin-tpk-to-zpk
overview: Add the Thales CA/CB Translate PIN from TPK to ZPK command as a new gRPC RPC using the existing HsmCommand framework.
todos:
  - id: proto
    content: Add TranslatePinFromTpkToZpk RPC and messages to hsm.proto (including destination_zpk)
    status: completed
  - id: regenerate
    content: Regenerate protobuf sources with mvn compile
    status: completed
  - id: command
    content: Update TranslatePinFromTpkToZpkCaCommand to include destination_zpk packing and validation
    status: completed
  - id: grpc
    content: Ensure translatePinFromTpkToZpk method in HsmGrpcService uses the updated proto message
    status: completed
  - id: tests
    content: Update unit tests for TranslatePinFromTpkToZpkCaCommand to include destination_zpk
    status: completed
  - id: verify
    content: Run mvn test and fix any regressions
    status: completed
isProject: false
---

# Plan: Add CA/CB Translate PIN from TPK to ZPK command

Add the Thales CA/CB Translate PIN from TPK to ZPK command as a new gRPC RPC, following the field specification in `.cursor/requirements/TranslatePinFromTPKToZPK.md`. The implementation will use the existing `HsmCommand` framework.

## Scope
- Support the exact CA/CB fields listed in the requirement:
  - Request: `Source TPK`, `Destination ZPK`, `Minimum PIN Length`, `Source PIN Block`, `Source PIN Block Format`, `Destination PIN Block Format`, `PAN/Token`.
  - Response: `PIN Length`, `Destination PIN Block`, `Source PIN Block Format`.
- The Source TPK is passed as a combined scheme + value string (e.g. `UE685...` for U+32H, `TE685...` for T+48H, or a plain 16-hex single-DES TPK).
- The Destination PIN Block length is inferred from the response by stripping the fixed 2-char PIN length at the start and the fixed 2-char source format at the end, so both DES (16 hex) and AES (32 hex) destination blocks are handled without extra configuration.
- Out of scope: key block formats, multiple LMK identifiers, message trailer, and any fields not listed in the requirement.

## Proposed Changes

1. **Extend `src/main/proto/hsm.proto`**
   - Add `rpc TranslatePinFromTpkToZpk(TranslatePinFromTpkToZpkRequest) returns (TranslatePinFromTpkToZpkResponse);` to `HsmService`.
   - Add `message TranslatePinFromTpkToZpkRequest` with fields:
     - `string source_tpk = 1;`
     - `string destination_zpk = 2;`
     - `string minimum_pin_length = 3;`
     - `string source_pin_block = 4;`
     - `string source_pin_block_format = 5;`
     - `string destination_pin_block_format = 6;`
     - `string pan_token = 7;`
   - Add `message TranslatePinFromTpkToZpkResponse` with fields:
     - `string error_code = 1;`
     - `string pin_length = 2;`
     - `string destination_pin_block = 3;`
     - `string source_pin_block_format = 4;`

2. **Regenerate protobuf sources**
   - Run `mvn compile` so the new request/response classes and service stubs are generated.

3. **Create `src/main/java/com/company/cvhsm/hsm/command/TranslatePinFromTpkToZpkCaCommand.java`**
   - Implement `HsmCommand<TranslatePinFromTpkToZpkRequest, TranslatePinFromTpkToZpkResponse>`.
   - Return command code `CA` and response code `CB`.
   - `packRequestBody`:
     - Validate `minimum_pin_length` is 2 numeric chars between `04` and `12`.
     - Validate `source_pin_block` is 16 or 32 hex characters.
     - Validate `source_pin_block_format` and `destination_pin_block_format` are 2 numeric chars.
     - Validate `pan_token` is non-empty and numeric.
     - Validate `source_tpk` and `destination_zpk` are non-empty and match one of the supported formats: 16 hex chars, `U` + 32 hex chars, or `T` + 48 hex chars.
     - Write the fields in order as ASCII bytes: `source_tpk + destination_zpk + minimum_pin_length + source_pin_block + source_pin_block_format + destination_pin_block_format + pan_token`.
   - `unpackResponseBody`:
     - If the body is empty, return a response with only the error code.
     - Convert the body to ASCII; if it is shorter than 4 characters, throw an `IllegalArgumentException`.
     - Parse `pin_length` from the first 2 characters.
     - Parse `source_pin_block_format` from the last 2 characters.
     - Treat the characters between them as `destination_pin_block`.

4. **Expose the new RPC in `src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java`**
   - Add `translatePinFromTpkToZpk(TranslatePinFromTpkToZpkRequest, StreamObserver<TranslatePinFromTpkToZpkResponse>)` that resolves the `CA` command via `HsmCommandFactory` and invokes it through `HsmCommandInvoker`, mirroring the existing methods.

5. **Write `src/test/java/com/company/cvhsm/hsm/command/TranslatePinFromTpkToZpkCaCommandTest.java`**
   - Test that command/response codes are `CA`/`CB`.
   - Test packing a request body with U-scheme source TPK and destination ZPK, plus a 16-hex source PIN block.
   - Test validation rejects invalid minimum PIN length, non-hex PIN block, invalid formats, missing PAN/token, and invalid ZPK format.
   - Test unpacking a successful CB response with a 16-hex destination PIN block.
   - Test unpacking a successful CB response with a 32-hex AES destination PIN block.
   - Test that an empty response body returns only the error code.
   - Test that a too-short response body is rejected.

6. **Verify the build**
   - Run `mvn test` to ensure the new command and all existing tests pass. Note: `GenerateKeyA0CommandTest.unpacksResponseBody` currently has pre-existing errors in `GenerateKeyA0Command.java` that the user is handling separately; those should not be touched as part of this plan.

## Files to Change
- `[src/main/proto/hsm.proto](src/main/proto/hsm.proto)`
- `[src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java](src/main/java/com/company/cvhsm/grpc/HsmGrpcService.java)`
- `[src/main/java/com/company/cvhsm/hsm/command/TranslatePinFromTpkToZpkCaCommand.java](src/main/java/com/company/cvhsm/hsm/command/TranslatePinFromTpkToZpkCaCommand.java)` (new)
- `[src/test/java/com/company/cvhsm/hsm/command/TranslatePinFromTpkToZpkCaCommandTest.java](src/test/java/com/company/cvhsm/hsm/command/TranslatePinFromTpkToZpkCaCommandTest.java)` (new)

## Out of Scope
- Key block or TR-31 key formats for the TPK/ZPK.
- LMK identifier (`%`) or message trailer handling.
- Modifying `GenerateKeyA0Command.java` or its tests.