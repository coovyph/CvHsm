package com.company.cvhsm.hsm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.company.cvhsm.grpc.TranslatePinRequest;

@Component
public class TranslatePINCCCmd extends TranslatePINCACmd{
	public TranslatePINCCCmd(@Value("${hsm.cmd.type:variant}") String cmdType) {
		super(cmdType);
	}

	private static final String COMMAND_CODE = "CC";
	private static final String RESPONSE_CODE = "CD";

	@Override
	public String getCommandCode() {
		return COMMAND_CODE;
	}

	@Override
	public String getResponseCode() {
		return RESPONSE_CODE;
	}


	public byte[] packRequestBody(TranslatePinRequest request) {
		TranslatePinRequest fReq = request.toBuilder().setMinimumPinLength("12").build();
		return super.packRequestBody(fReq);
	}

}
