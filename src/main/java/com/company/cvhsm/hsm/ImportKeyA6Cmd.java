package com.company.cvhsm.hsm;

import java.nio.charset.StandardCharsets;

import com.company.cvhsm.grpc.ImportKeyRequest;
import com.company.cvhsm.grpc.ImportKeyResponse;

public abstract class ImportKeyA6Cmd extends BaseHsmCommand<ImportKeyRequest, ImportKeyResponse>{
	private static final String COMMAND_CODE = "A6";
	private static final String RESPONSE_CODE = "A7";
	private static final int KCV_LENGTH = 6;

	@Override
	public String getCommandCode() {
		return COMMAND_CODE;
	}

	@Override
	public String getResponseCode() {
		return RESPONSE_CODE;
	}

	@Override
	public byte[] packRequestBody(ImportKeyRequest request) {
		validateRequest(request);

		byte[] rslt = new byte[totalPackBytes(request)];

		int offset = 0;
		System.arraycopy(request.getKeyType().getBytes(), 0, rslt, offset, 3);
		offset+=3;

		byte[] zmkBytes = request.getZmk().getBytes();
		System.arraycopy(zmkBytes, 0, rslt, offset, zmkBytes.length);
		offset+=zmkBytes.length;

		byte[] keyUnderZmkBytes = request.getKeyUnderZmk().getBytes();
		System.arraycopy(keyUnderZmkBytes, 0, rslt, offset , keyUnderZmkBytes.length);
		offset+=keyUnderZmkBytes.length;

		rslt[offset] = (byte)request.getKeySchemeLmk().charAt(0);
		offset++;

		packAdditionalBytes(request,rslt,offset);		

		return rslt;
	}

	protected abstract void packAdditionalBytes(ImportKeyRequest request,byte[] rslt,int offset);

	protected abstract int totalPackBytes(ImportKeyRequest request);


	protected void validateRequest(ImportKeyRequest request) {
		requireLength(request.getKeyType(), "key type", 3);
		requireLength(request.getKeySchemeLmk(), "LMK key scheme", 1);
		requireNonEmpty(request.getZmk(), "ZMK");
		requireNonEmpty(request.getKeyUnderZmk(), "key under ZMK");
	}

	@Override
	public ImportKeyResponse unpackResponseBody(String errorCode, Object cache, byte[] body) {
		if (body.length == 0) {
            return ImportKeyResponse.newBuilder()
                    .setErrorCode(errorCode)
                    .build();
        }

        String response = new String(body, StandardCharsets.US_ASCII);
        if (response.length() < KCV_LENGTH) {
            throw new IllegalArgumentException(
                    "A7 response body is too short: expected at least KCV, got " + response.length() + " chars");
        }

        int keyUnderLmkEnd = response.length() - KCV_LENGTH;
        String keyUnderLmk = response.substring(0, keyUnderLmkEnd);
        String keyCheckValue = response.substring(keyUnderLmkEnd);

        return ImportKeyResponse.newBuilder()
                .setErrorCode(errorCode)
                .setKeyUnderLmk(keyUnderLmk)
                .setKeyCheckValue(keyCheckValue)
                .build();
	}
	
	@Override
	public Object createCache(ImportKeyRequest request) {
		return null;
	}


}
