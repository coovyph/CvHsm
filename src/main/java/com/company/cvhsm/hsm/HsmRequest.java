package com.company.cvhsm.hsm;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents a Thales HSM host command request before MLI framing is applied.
 *
 * <p>Wire format after framing: 2-byte MLI + 4-char header + 2-char command code + body.
 */
@Getter
@Builder
public class HsmRequest {

	String header;
	String commandCode;
	byte[] body;
	Object cache;

	public void setCache(Object inCache) {
		this.cache = inCache;
	}
	public int getMessageLength() {
		return header.length() + commandCode.length() + body.length;
	}
}
