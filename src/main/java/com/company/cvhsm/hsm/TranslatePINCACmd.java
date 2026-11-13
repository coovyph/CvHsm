package com.company.cvhsm.hsm;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.company.cvhsm.grpc.TranslatePinFromTpkToZpkRequest;
import com.company.cvhsm.grpc.TranslatePinFromTpkToZpkResponse;

@Component
public class TranslatePINCACmd extends BaseHsmCommand<TranslatePinFromTpkToZpkRequest, TranslatePinFromTpkToZpkResponse>{
	private static final String COMMAND_CODE = "CA";
	private static final String RESPONSE_CODE = "CB";
	private static final int PIN_LENGTH_FIELD_SIZE = 2;
	private static final int PIN_BLOCK_FORMAT_FIELD_SIZE = 2;
	private static final int MINIMUM_RESPONSE_BODY_LENGTH = PIN_LENGTH_FIELD_SIZE + PIN_BLOCK_FORMAT_FIELD_SIZE;


	private boolean variant;
	public TranslatePINCACmd(@Value("${hsm.cmd.type:variant}") String cmdType) {
		this.variant = "variant".equalsIgnoreCase(cmdType);
	}

	@Override
	public String getCommandCode() {
		return COMMAND_CODE;
	}

	@Override
	public String getResponseCode() {
		return RESPONSE_CODE;
	}

	@Override
	public byte[] packRequestBody(TranslatePinFromTpkToZpkRequest request) {
		validateRequest(request);
		int totalLgth = 6; // pin length (2 char) + source pin format(2 char) + dest pin format(2 char)
		if(this.variant) {
			char keyScheme = request.getSourceTpk().charAt(0);
			totalLgth += keyHexLength(keyScheme); 

			if(keyScheme=='Z' || 
					keyScheme=='U' ||
					keyScheme=='T') totalLgth ++;

			keyScheme = request.getDestinationZpk().charAt(0);
			totalLgth += keyHexLength(keyScheme); 

			if(keyScheme=='Z' || 
					keyScheme=='U' ||
					keyScheme=='T') totalLgth ++;

		}else {
			totalLgth += keyBlockLength(request.getSourceTpk()) + 1;
			totalLgth += keyBlockLength(request.getDestinationZpk()) + 1;
		}

		//the pinblock 
		totalLgth+=16;
		//the pin block it self
		if("48".equals(request.getSourcePinBlockFormat())) {
			//AES Pin block is 32
			totalLgth+=16;
			totalLgth+=19; //token digit
		}else if("04".equals(request.getSourcePinBlockFormat())) {
			totalLgth+=18;
		}else {
			totalLgth+=12;
		}
		

		ByteBuffer buff = ByteBuffer.allocate(totalLgth);

		buff.put(request.getSourceTpk().getBytes());
		buff.put(request.getDestinationZpk().getBytes());
		buff.put(request.getMinimumPinLength().getBytes());
		buff.put(request.getSourcePinBlock().getBytes());
		buff.put(request.getSourcePinBlockFormat().getBytes());
		buff.put(request.getDestinationPinBlockFormat().getBytes());
		buff.put(request.getPanToken().getBytes());

		byte[] rslt = new byte[buff.position()];
		buff.flip();

		buff.get(rslt);

		return rslt;

	}


	private void validateRequest(TranslatePinFromTpkToZpkRequest request) {

		requireNonEmpty(request.getSourceTpk(), "source TPK");
		validateKeyFormat(request.getSourceTpk(), "source TPK", this.variant);

		requireNonEmpty(request.getDestinationZpk(), "destination ZPK");
		validateKeyFormat(request.getDestinationZpk(), "destination ZPK", this.variant);


		requireLength(request.getMinimumPinLength(), "minimum PIN length", 2);
		requireIntegerBetween(request.getMinimumPinLength(), "minium PIN length",4,12);
	
		requireNonEmpty(request.getSourcePinBlock(), "source PIN block");

		requireLength(request.getSourcePinBlockFormat(), "source PIN block format", 2);

		requireLength(request.getDestinationPinBlockFormat(), "destination PIN block format", 2);
		requireNonEmpty(request.getPanToken(), "PAN/token");
	}



	@Override
	public TranslatePinFromTpkToZpkResponse unpackResponseBody(String errorCode, Object cache, byte[] body) {
		if (body.length == 0) {
			return TranslatePinFromTpkToZpkResponse.newBuilder()
					.setErrorCode(errorCode)
					.build();
		}

		String response = new String(body, StandardCharsets.US_ASCII);
		if (response.length() < MINIMUM_RESPONSE_BODY_LENGTH) {
			throw new IllegalArgumentException(
					"CB response body is too short: expected at least " + MINIMUM_RESPONSE_BODY_LENGTH
					+ " chars, got " + response.length());
		}

		String pinLength = response.substring(0, PIN_LENGTH_FIELD_SIZE);
		String sourcePinBlockFormat = response.substring(response.length() - PIN_BLOCK_FORMAT_FIELD_SIZE);
		String destinationPinBlock = response.substring(PIN_LENGTH_FIELD_SIZE, response.length() - PIN_BLOCK_FORMAT_FIELD_SIZE);

		return TranslatePinFromTpkToZpkResponse.newBuilder()
				.setErrorCode(errorCode)
				.setPinLength(pinLength)
				.setDestinationPinBlock(destinationPinBlock)
				.setSourcePinBlockFormat(sourcePinBlockFormat)
				.build();
	}
}
