package com.company.cvhsm.hsm;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents a Thales HSM host command response after MLI framing is removed.
 *
 * <p>Wire format after deframing: 4-char header + 2-char response code + 2-char error code + body.
 */
@Getter
@Builder
public class HsmResponse {

	String header;
	String responseCode;
	String errorCode;
	byte[] body;
	private Object cache;

	public void setCache(Object inCache) {
		this.cache = inCache;
	}
}
