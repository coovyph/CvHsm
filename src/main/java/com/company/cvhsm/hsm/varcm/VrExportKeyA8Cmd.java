package com.company.cvhsm.hsm.varcm;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.company.cvhsm.config.OnVariant;
import com.company.cvhsm.grpc.ExportKeyRequest;
import com.company.cvhsm.grpc.ExportKeyResponse;
import com.company.cvhsm.hsm.ExportKeyA8Cmd;

/**
 * Packs and unpacks the Thales A8/A9 Export a Key command.
 *
 * <p>Supports the base fields for exporting a key encrypted under the LMK to ZMK or TMK encryption.
 * The ZMK/TMK and the key-under-LMK are passed as combined scheme + value strings, e.g.
 * {@code UE685...} or {@code U5F2D...}, matching the A8 command specification.
 *
 * <p>Request body format:
 * <pre>
 * Key Type (3 chars) + ';' + ZMK/TMK Flag (1 char) + ZMK/TMK (scheme + value) + Key (scheme + value)
 * + Key Scheme (ZMK/TMK) (1 char)
 * </pre>
 *
 * <p>Response body format (after the error code has been stripped):
 * <pre>
 * Key (under ZMK/TMK) (scheme + value) + Key Check Value (6 hex chars)
 * </pre>
 */
@Component
@OnVariant
public class VrExportKeyA8Cmd extends ExportKeyA8Cmd {

	@Override
	public byte[] packRequestBody(ExportKeyRequest request) {
		validateRequest(request);
		ByteBuffer buff = ByteBuffer.allocate(104);//assuming the max length is triple T

		buff.put(request.getKeyType().getBytes(StandardCharsets.US_ASCII));
		buff.put((byte)ZMK_TMK_DELIMITER);
		buff.put(request.getZmkTmkFlag().getBytes(StandardCharsets.US_ASCII));
		buff.put(request.getZmkTmk().getBytes(StandardCharsets.US_ASCII));
		buff.put(request.getKey().getBytes(StandardCharsets.US_ASCII));
		buff.put(request.getKeySchemeZmkTmk().getBytes(StandardCharsets.US_ASCII));

		byte[] rslt = new byte[buff.position()];
		buff.flip();

		buff.get(rslt);

		return rslt;

	}


	private  void validateRequest(ExportKeyRequest request) {
		requireLength(request.getKeyType(), "key type", 3);
		requireLength(request.getZmkTmkFlag(), "ZMK/TMK flag", 1);
		if (!"0".equals(request.getZmkTmkFlag()) && !"1".equals(request.getZmkTmkFlag())) {
			throw new IllegalArgumentException(
					"ZMK/TMK flag must be '0' (ZMK) or '1' (TMK), got: " + request.getZmkTmkFlag());
		}
		requireLength(request.getKeySchemeZmkTmk(), "ZMK/TMK key scheme", 1);
		requireNonEmpty(request.getZmkTmk(), "ZMK/TMK");
		requireNonEmpty(request.getKey(), "key");
	}




	@Override
	public ExportKeyResponse unpackResponseBody(String errorCode, Object cache, byte[] body) {
		if (body.length == 0) {
			return ExportKeyResponse.newBuilder()
					.setErrorCode(errorCode)
					.build();
		}

		String response = new String(body, StandardCharsets.US_ASCII);
		if (response.length() < KCV_LENGTH) {
			throw new IllegalArgumentException(
					"A9 response body is too short: expected at least KCV, got " + response.length() + " chars");
		}

		int keyEnd = response.length() - KCV_LENGTH;
		String keyUnderZmkTmk = response.substring(0, keyEnd);
		String keyCheckValue = response.substring(keyEnd);

		return ExportKeyResponse.newBuilder()
				.setErrorCode(errorCode)
				.setKeyUnderZmkTmk(keyUnderZmkTmk)
				.setKeyCheckValue(keyCheckValue)
				.build();
	}

	@Override
	public Object createCache(ExportKeyRequest request) {
		//no cache required
		return null;
	}
}
