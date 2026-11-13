package com.company.cvhsm.hsm;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.company.cvhsm.grpc.GenerateKeyRequest;
import com.company.cvhsm.grpc.GenerateKeyResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class GenerateKeyA0Command extends BaseHsmCommand<GenerateKeyRequest, GenerateKeyResponse> {

	private static final String COMMAND_CODE = "A0";
	private static final String RESPONSE_CODE = "A1";
	private static final char EXPORT_DELIMITER = ';';
	public static final int KCV_LENGTH = 6;
	@Override
	public String getCommandCode() {
		return COMMAND_CODE;
	}
	@Override
	public String getResponseCode() {
		return RESPONSE_CODE;
	}

	@Override
	public byte[] packRequestBody(GenerateKeyRequest request) {
		validateRequest(request);

		ByteBuffer buff = prepareBuffer();
		buff.put((byte)request.getMode().charAt(0));
		buff.put(request.getKeyType().getBytes(StandardCharsets.US_ASCII));
		buff.put((byte)request.getKeySchemeLmk().charAt(0));

		buff.put((byte)EXPORT_DELIMITER);
		buff.put((byte)request.getZmkTmkFlag().charAt(0));
		buff.put(request.getZmk().getBytes(StandardCharsets.US_ASCII));
		buff.put((byte)request.getKeySchemeZmk().charAt(0));

		//need to write exportability option
		constructAdditionalRequest(request,buff);

		byte[] rslt = new byte[buff.position()];

		buff.flip();
		buff.get(rslt);

		
		return rslt;
	}
	
	@Override
	public GenerateKeyResponse unpackResponseBody(String errorCode, Object cache, byte[] body) {
		if (body.length == 0) {
			return GenerateKeyResponse.newBuilder()
					.setErrorCode(errorCode).build();
		}


		String response = new String(body, StandardCharsets.US_ASCII);
		if (response.length() < 1 + KCV_LENGTH) {
			throw new IllegalArgumentException(
					"A1 response body is too short: expected at least LMK scheme + KCV, got " + response.length()
					+ " chars");
		}

		int lmkKeyLength = calculateRespKeyLength(response);
		int offset = 0;
		int lastOffset = lmkKeyLength + offset + 1;
		if(lmkKeyLength == 16 && response.charAt(offset) != 'Z') {
			lastOffset--;
		}
		String keyUnderLmk = response.substring(offset,lastOffset);	
		offset=lastOffset;

		String keyUnderZmk = null;
		boolean hasKeyUnderZmk = (cache instanceof String mode && ("1".equals(mode) || "B".equals(mode)));

		if(hasKeyUnderZmk) {
			int keyUnderZmkLength = keyHexLength(response.charAt(offset));
			lastOffset = keyUnderZmkLength + offset + 1;
			//single length DES with Z as key scheme
			if(keyUnderZmkLength == 16 && response.charAt(offset) != 'Z') {
				lastOffset --;
			}
			
			keyUnderZmk = response.substring(offset,lastOffset);
			offset = lastOffset;
		}

		String keyCheckValue = response.substring(offset,offset+KCV_LENGTH);

		return GenerateKeyResponse.newBuilder().
				setErrorCode(errorCode).
				setKeyUnderLmk(keyUnderLmk).
				setKeyUnderZmk(keyUnderZmk).
				setKeyCheckValue(keyCheckValue).
				build();

	}
	
	protected abstract int calculateRespKeyLength(String response);

	protected abstract ByteBuffer prepareBuffer();
	protected abstract void constructAdditionalRequest(GenerateKeyRequest request,ByteBuffer buff);


	protected abstract void validateKeyType(String keyType);
	protected abstract void validateKeySchemeLmk(String keySchemeLmk);
	protected abstract void validateMoreFields(GenerateKeyRequest request);

	protected void validateRequest(GenerateKeyRequest request) {
		valuesIn(request.getMode(),"mode", false, "0","1");
		validateKeyType(request.getKeyType());
		validateKeySchemeLmk(request.getKeySchemeLmk());
		valuesIn(request.getZmkTmkFlag(), "zmk/tmk flag",false, "0", "1");
		validateMoreFields(request);
	}

	@Override
	public Object createCache(GenerateKeyRequest request) {
		return request.getMode();
	}

}
