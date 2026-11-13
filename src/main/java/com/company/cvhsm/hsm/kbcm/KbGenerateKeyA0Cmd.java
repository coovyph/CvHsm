package com.company.cvhsm.hsm.kbcm;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

import com.company.cvhsm.config.OnKeyblock;
import com.company.cvhsm.grpc.GenerateKeyRequest;
import com.company.cvhsm.hsm.GenerateKeyA0Command;

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
@OnKeyblock
public class KbGenerateKeyA0Cmd extends GenerateKeyA0Command {

	
	@Override
	protected ByteBuffer prepareBuffer() {
		return ByteBuffer.allocate(136);
	}



	@Override
	protected void validateKeyType(String keyType) {
		valueEquals(keyType, "key type", false, "FFF");
	}

	@Override
	protected void validateKeySchemeLmk(String keySchemeLmk) {
		valueEquals(keySchemeLmk, "key scheme lmk", false, "S");
	}

	@Override
	protected void validateMoreFields(GenerateKeyRequest request) {
	}

	@Override
	protected void constructAdditionalRequest(GenerateKeyRequest request, ByteBuffer buff) {
		buff.put((byte)'#');
		buff.put(request.getKeyUsage().getBytes());
		buff.put(request.getAlgorithm().getBytes());
		buff.put((byte)request.getModeOfUse().charAt(0));
		buff.put(request.getKeyVersionNumber().getBytes());
		buff.put(request.getExportability().getBytes());
		buff.put(new byte[] {0x30,0x30});
	}

	@Override
	protected int calculateRespKeyLength(String response) {
		return keyBlockLength(response);
	}



}
