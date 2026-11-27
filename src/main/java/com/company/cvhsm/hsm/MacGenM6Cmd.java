package com.company.cvhsm.hsm;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Service;

import com.company.cvhsm.grpc.MacGenRequest;
import com.company.cvhsm.grpc.MacGenResponse;

@Service
public  class MacGenM6Cmd extends BaseHsmCommand<MacGenRequest, MacGenResponse>{

	private static final String COMMAND_CODE = "M6";
	private static final String RESPONSE_CODE = "M7";

	public static final char DELIMITER = ';';
	@Override
	public String getCommandCode() {
		return COMMAND_CODE;
	}

	@Override
	public String getResponseCode() {
		return RESPONSE_CODE;
	}

	@Override
	public MacGenResponse unpackResponseBody(String errorCode, Object cache, byte[] body) {
		if (body.length == 0) {
			return MacGenResponse.newBuilder()
					.setErrorCode(errorCode)
					.build();
		}

		return MacGenResponse.newBuilder()
				.setErrorCode(errorCode)
				.setMac(new String(body)).build();
	}

	
	@Override
	public byte[] packRequestBody(MacGenRequest request) {
		int msgLgth = request.getMessage().length();
		int lgth = 12 + request.getKey().length() + msgLgth;
		ByteBuffer buff = ByteBuffer.allocate(lgth);
		buff.put((byte)(request.getModeFlag() + 48));
		buff.put((byte)(request.getInputFormat() + 48));
		buff.put((byte)(request.getMacSize()+ 48));
		buff.put((byte)(request.getMacAlgo()+ 48));
		buff.put((byte)(request.getPadMethod()+ 48));
		buff.put(request.getKeyType().getBytes());
		buff.put(request.getKey().getBytes());
		
		String lgthId = Integer.toHexString(msgLgth).toUpperCase();
		lgthId = String.format("%4s", lgthId);
		lgthId = lgthId.replaceAll(" ", "0");
		
		buff.put(lgthId.getBytes());
		buff.put(request.getMessage().getBytes());
		
		byte[] rslt = new byte[buff.position()];
		
		buff.flip();
		
		buff.get(rslt);
		
		return rslt;
	}
}
