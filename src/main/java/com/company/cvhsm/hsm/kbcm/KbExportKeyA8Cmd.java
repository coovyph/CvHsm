package com.company.cvhsm.hsm.kbcm;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.company.cvhsm.config.OnKeyblock;
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
 * Key Type (3 chars) +  ZMK/TMK (scheme + value) + Key (scheme + value)
 * + Key Scheme (ZMK/TMK) (1 char)
 * </pre>
 *
 * <p>Response body format (after the error code has been stripped):
 * <pre>
 * Key (under ZMK/TMK) (scheme + value) + Key Check Value (6 hex chars)
 * </pre>
 */
@Component
@OnKeyblock
public class KbExportKeyA8Cmd extends ExportKeyA8Cmd {

	@Override
	public byte[] packRequestBody(ExportKeyRequest request) {
		validateRequest(request);
		int size = 4; //key type + lmk key scheme + key scheme tmk;
		int zmkTmkLgth = keyBlockLength(request.getZmkTmk()) + 1; //plus 1 char keyscheme S
		int keyLgth = keyBlockLength(request.getKey()) + 1; 
		size+=zmkTmkLgth;
		size+=keyLgth;

		byte[] rslt = new byte[size];

		int offset = 0;
		System.arraycopy("FFF".getBytes(), 0, rslt, offset, 3);
		offset+=3;

		System.arraycopy(request.getZmkTmk().getBytes(), 0, rslt, offset, zmkTmkLgth);
		offset+=zmkTmkLgth;

		System.arraycopy(request.getKey().getBytes(), 0, rslt, offset, keyLgth);
		offset+=keyLgth;

		rslt[offset] = (byte)request.getKeySchemeZmkTmk().charAt(0);

		return rslt;

	}


	private  void validateRequest(ExportKeyRequest request) {
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
