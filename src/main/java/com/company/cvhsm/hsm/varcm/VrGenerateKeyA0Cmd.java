package com.company.cvhsm.hsm.varcm;

import com.company.cvhsm.config.OnVariant;
import com.company.cvhsm.grpc.GenerateKeyRequest;
import com.company.cvhsm.grpc.GenerateKeyResponse;
import com.company.cvhsm.hsm.GenerateKeyA0Command;
import com.company.cvhsm.hsm.HsmCommand;

import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Packs and unpacks the Thales A0/A1 Generate Key command.
 *
 * <p>
 * Supports Mode {@code 1} (generate key and encrypt under ZMK) with a
 * configurable key type,
 * LMK key scheme, ZMK value, and ZMK key scheme. The first implementation is
 * targeted at
 * Key Type {@code 001} (ZPK) and Key Scheme {@code U}.
 *
 * <p>
 * Request body format for Mode {@code 1}:
 * 
 * <pre>
 * Mode (1 char) + Key Type (3 chars) + Key Scheme LMK (1 char)
 * + ';' + ZMK/TMK Flag (1 char, {@code
 * 0
 * } = ZMK, {@code
 * 1
 * } = TMK) + ZMK (hex string) + Key Scheme ZMK (1 char)
 * </pre>
 *
 * <p>
 * Response body format (after the error code has been stripped):
 * 
 * <pre>
 * Key Scheme LMK (1 char) + Key Under LMK (hex) + Key Under ZMK (hex) + KCV (6 hex chars)
 * </pre>
 */
@Component
@OnVariant
public class VrGenerateKeyA0Cmd extends GenerateKeyA0Command{

	@Override
	protected int calculateRespKeyLength(String response) {
		return keyHexLength(response.charAt(0));
	}

	@Override
	protected ByteBuffer prepareBuffer() {
		return ByteBuffer.allocate(41);
	}

	@Override
	protected void constructAdditionalRequest(GenerateKeyRequest request, ByteBuffer buff) {
		//no additional request bytes required
	}

	@Override
	protected void validateKeyType(String keyType) {
		requireLength(keyType, "key type", 3);
	}

	@Override
	protected void validateKeySchemeLmk(String keySchemeLmk) {
		
	}

	@Override
	protected void validateMoreFields(GenerateKeyRequest request) {
		//no more fields validated
	}

}
