package com.company.cvhsm.hsm.varcm;

import com.company.cvhsm.config.OnKeyblock;
import com.company.cvhsm.config.OnVariant;
import com.company.cvhsm.grpc.ImportKeyRequest;
import com.company.cvhsm.grpc.ImportKeyResponse;
import com.company.cvhsm.hsm.HsmCommand;
import com.company.cvhsm.hsm.ImportKeyA6Cmd;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Packs and unpacks the Thales A6/A7 Import a Key command.
 *
 * <p>Supports the base fields for importing a key encrypted under a ZMK into LMK encryption.
 * The ZMK and the key-under-ZMK are passed as combined scheme + value strings, e.g. {@code UE685...}
 * or {@code XAC4D...}, matching the A6 command specification.
 *
 * <p>Request body format:
 * <pre>
 * Key Type (3 chars) + ZMK (scheme + value) + Key (ZMK) (scheme + value) + Key Scheme (LMK) (1 char)
 * </pre>
 *
 * <p>Response body format (after the error code has been stripped):
 * <pre>
 * Key (under LMK) (scheme + value) + Key Check Value (6 hex chars)
 * </pre>
 */
@Component
@OnVariant
public class VrImportKeyA6Cmd extends ImportKeyA6Cmd {

	@Override
	protected void packAdditionalBytes(ImportKeyRequest request, byte[] rslt, int offset) {
		//do nothing
	}

	@Override
	protected int totalPackBytes(ImportKeyRequest request) {
		// key type + lmk ke scheme
		int lgth = 4;

		int size = keyHexLength(request.getZmk().charAt(0)) + 1;
		if(size == 17 && request.getZmk().charAt(0)!='Z') {
			size--;
		}
		lgth+=size;
		
		
		size = keyHexLength(request.getKeyUnderZmk().charAt(0)) + 1;
		if(size == 17 && request.getZmk().charAt(0)!='Z') {
			size--;
		}
		lgth+=size;

		return lgth;
	}   


}
