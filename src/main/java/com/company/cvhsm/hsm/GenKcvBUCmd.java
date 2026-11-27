package com.company.cvhsm.hsm;

import org.springframework.stereotype.Service;

import com.company.cvhsm.grpc.GenKcvRequest;
import com.company.cvhsm.grpc.GenKcvResponse;

@Service
public  class GenKcvBUCmd extends BaseHsmCommand<GenKcvRequest, GenKcvResponse>{

	private static final String COMMAND_CODE = "BU";
	private static final String RESPONSE_CODE = "BV";

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
	public GenKcvResponse unpackResponseBody(String errorCode, Object cache, byte[] body) {
		if (body.length == 0) {
			return GenKcvResponse.newBuilder()
					.setErrorCode(errorCode)
					.build();
		}

		return GenKcvResponse.newBuilder().
				setKcv(new String(body)).
				setErrorCode(errorCode).
				build();

	}

	@Override
	public byte[] packRequestBody(GenKcvRequest request) {
		//11 constant bytes length for key type informations
		int rkeyLgth = request.getKey().length();
		int lgth = rkeyLgth + 11; 
		byte[] rqstBytes = new byte[lgth];
		int offset = 0;
		rqstBytes[offset] = 'F';
		offset++;
		rqstBytes[offset] = 'F';
		offset++;
		rqstBytes[offset] = (byte)request.getKeyLgth().charAt(0);
		offset++;

		System.arraycopy(request.getKey().getBytes(),0, rqstBytes, offset, rkeyLgth);
		offset+=rkeyLgth;

		rqstBytes[offset] = ';';
		offset++;

		System.arraycopy(request.getKeyType().getBytes(), 0, rqstBytes, offset, 3);
		offset+=3;

		rqstBytes[offset] = ';';
		offset++;
		rqstBytes[offset] = '0';
		offset++;
		rqstBytes[offset] = '0';
		offset++;
		rqstBytes[offset] = '1'; //6 digits KCV

		return rqstBytes;
	}

}
