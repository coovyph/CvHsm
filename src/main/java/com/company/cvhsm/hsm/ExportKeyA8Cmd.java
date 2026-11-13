package com.company.cvhsm.hsm;

import java.nio.charset.StandardCharsets;

import com.company.cvhsm.grpc.ExportKeyRequest;
import com.company.cvhsm.grpc.ExportKeyResponse;

public abstract class ExportKeyA8Cmd extends BaseHsmCommand<ExportKeyRequest, ExportKeyResponse>{

	private static final String COMMAND_CODE = "A8";
	private static final String RESPONSE_CODE = "A9";

	public static final char ZMK_TMK_DELIMITER = ';';
	@Override
	public String getCommandCode() {
		return COMMAND_CODE;
	}

	@Override
	public String getResponseCode() {
		return RESPONSE_CODE;
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

}
