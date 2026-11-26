package com.company.cvhsm.hsm;

import org.springframework.stereotype.Component;

import com.company.cvhsm.grpc.HsmStatusRequest;
import com.company.cvhsm.grpc.HsmStatusResponse;

@Component
public class HsmStatusNOCmd extends BaseHsmCommand<HsmStatusRequest, HsmStatusResponse>{
	private static final String COMMAND_CODE = "NO";
	private static final String RESPONSE_CODE = "NP";

	@Override
	public String getCommandCode() {
		return COMMAND_CODE;
	}

	@Override
	public String getResponseCode() {
		return RESPONSE_CODE;
	}

	@Override
	public byte[] packRequestBody(HsmStatusRequest request) {
		byte[] rslt = new byte[2];
		rslt[0] = '0';
		rslt[1] = '0';

		return rslt;
	}


	@Override
	public HsmStatusResponse unpackResponseBody(String errorCode, Object cache, byte[] body) {
		
        return HsmStatusResponse.newBuilder()
                .setErrorCode(errorCode)
                .setStatusDesc(new String(body))
                .build();
	}
	
	@Override
	public Object createCache(HsmStatusRequest request) {
		return null;
	}


}
