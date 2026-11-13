package com.company.cvhsm.hsm.varcm;

import com.company.cvhsm.grpc.GenerateKeyRequest;
import com.company.cvhsm.grpc.GenerateKeyResponse;
import com.company.cvhsm.hsm.varcm.VrGenerateKeyA0Cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VrGenerateKeyA0CmdTest {

	private final VrGenerateKeyA0Cmd command = new VrGenerateKeyA0Cmd();

	@Test
	void returnsCommandAndResponseCodes() {
		assertEquals("A0", command.getCommandCode());
		assertEquals("A1", command.getResponseCode());
	}

	@Test
	void packsMode1RequestBodyWithZmkFlag() {
		String zmk = "E68586760A163026C29710073AB2D7BE";
		GenerateKeyRequest request = GenerateKeyRequest.newBuilder()
				.setMode("1")
				.setKeyType("001")
				.setKeySchemeLmk("U")
				.setZmkTmkFlag("0")
				.setZmk(zmk)
				.setKeySchemeZmk("U")
				.build();

		byte[] expectedPrefix = "1001U;0".getBytes(StandardCharsets.US_ASCII);
		byte[] expectedZmk = zmk.getBytes(StandardCharsets.US_ASCII);
		byte[] expectedSuffix = "U".getBytes(StandardCharsets.US_ASCII);

		byte[] body = command.packRequestBody(request);

		byte[] expected = new byte[expectedPrefix.length + expectedZmk.length + expectedSuffix.length];
		System.arraycopy(expectedPrefix, 0, expected, 0, expectedPrefix.length);
		System.arraycopy(expectedZmk, 0, expected, expectedPrefix.length, expectedZmk.length);
		System.arraycopy(expectedSuffix, 0, expected, expectedPrefix.length + expectedZmk.length, expectedSuffix.length);

		assertArrayEquals(expected, body);
	}

	@Test
	void packsMode1RequestBodyWithTmkFlag() {
		String zmk = "E68586760A163026C29710073AB2D7BE";
		GenerateKeyRequest request = GenerateKeyRequest.newBuilder()
				.setMode("1")
				.setKeyType("001")
				.setKeySchemeLmk("U")
				.setZmkTmkFlag("1")
				.setZmk(zmk)
				.setKeySchemeZmk("U")
				.build();

		byte[] expectedPrefix = "1001U;1".getBytes(StandardCharsets.US_ASCII);
		byte[] expectedZmk = zmk.getBytes(StandardCharsets.US_ASCII);
		byte[] expectedSuffix = "U".getBytes(StandardCharsets.US_ASCII);

		byte[] body = command.packRequestBody(request);

		byte[] expected = new byte[expectedPrefix.length + expectedZmk.length + expectedSuffix.length];
		System.arraycopy(expectedPrefix, 0, expected, 0, expectedPrefix.length);
		System.arraycopy(expectedZmk, 0, expected, expectedPrefix.length, expectedZmk.length);
		System.arraycopy(expectedSuffix, 0, expected, expectedPrefix.length + expectedZmk.length, expectedSuffix.length);

		assertArrayEquals(expected, body);
	}

	@Test
	void rejectsInvalidZmkTmkFlag() {
		GenerateKeyRequest request = GenerateKeyRequest.newBuilder()
				.setMode("1")
				.setKeyType("001")
				.setKeySchemeLmk("U")
				.setZmkTmkFlag("2")
				.setZmk("E68586760A163026C29710073AB2D7BE")
				.setKeySchemeZmk("U")
				.build();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.packRequestBody(request));
		assertEquals("A0 zmk/tmk flag value is expected one of 0/1", exception.getMessage());
	}

	@Test
	void rejectsInvalidModeLength() {
		GenerateKeyRequest request = GenerateKeyRequest.newBuilder()
				.setMode("11")
				.setKeyType("001")
				.setKeySchemeLmk("U")
				.setZmkTmkFlag("0")
				.setZmk("E68586760A163026C29710073AB2D7BE")
				.setKeySchemeZmk("U")
				.build();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.packRequestBody(request));
		assertEquals("A0 mode value is expected one of 0/1", exception.getMessage());
	}


	@ParameterizedTest
	@CsvSource({
		"U, 32",
		", 16",
		"T, 48"
	})
	void unpacksResponseBody(String lmkScheme, int keyLength) {
		if(lmkScheme == null)lmkScheme = "";
		String keyUnderLmk = lmkScheme + "A".repeat(keyLength);
		String tsKeyScheme = "";
		if("U".equals(lmkScheme)) {
			tsKeyScheme = "X";
		}else if("T".equals(lmkScheme)) {
			tsKeyScheme = "Y";
		}
		String keyUnderZmk =  tsKeyScheme + "B".repeat(keyLength);
		String keyCheckValue = "C".repeat(6);
		byte[] body = (keyUnderLmk + keyUnderZmk + keyCheckValue).getBytes(StandardCharsets.US_ASCII);

		GenerateKeyResponse response = command.unpackResponseBody("00","1", body);

		assertEquals("00", response.getErrorCode());
		assertEquals(keyUnderLmk, response.getKeyUnderLmk());
		assertEquals(keyUnderZmk, response.getKeyUnderZmk());
		assertEquals(keyCheckValue, response.getKeyCheckValue());
	}

	@Test
	void rejectsShortResponseBody() {
		byte[] body = "U".getBytes(StandardCharsets.US_ASCII);

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.unpackResponseBody("00","1", body));
		assertEquals(
				"A1 response body is too short: expected at least LMK scheme + KCV, got 1 chars",
				exception.getMessage());
	}
}
