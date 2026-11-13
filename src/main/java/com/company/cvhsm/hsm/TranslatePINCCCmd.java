package com.company.cvhsm.hsm;

import org.springframework.stereotype.Component;

@Component
public class TranslatePINCCCmd extends TranslatePINCACmd{
	public TranslatePINCCCmd(String cmdType) {
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

}
