package com.company.cvhsm.hsm.varcm;

import com.company.cvhsm.grpc.ExportKeyRequest;
import com.company.cvhsm.grpc.ExportKeyResponse;
import com.company.cvhsm.hsm.varcm.VrExportKeyA8Cmd;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VrExportKeyA8CmdTest {

	private final VrExportKeyA8Cmd command = new VrExportKeyA8Cmd();

	@Test
	void returnsCommandAndResponseCodes() {
		assertEquals("A8", command.getCommandCode());
		assertEquals("A9", command.getResponseCode());
	}

	@Test
	void packsBaseRequestBodyWithZmkFlag() {
		String zmkTmk = "UE68586760A163026C29710073AB2D7BE";
		String key = "U5F2DC42E10C92B16BA54802314CE95F5";
		String zmkTmkFlag = "0";
		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setKeyType("001")
				.setZmkTmkFlag(zmkTmkFlag)
				.setZmkTmk(zmkTmk)
				.setKey(key)
				.setKeySchemeZmkTmk("U")
				.build();

		byte[] body = command.packRequestBody(request);

		byte[] expected = ("001;" + zmkTmkFlag + zmkTmk + key + "U").getBytes(StandardCharsets.US_ASCII);
		assertArrayEquals(expected, body);
	}

	@Test
	void packsBaseRequestBodyWithTmkFlag() {
		String zmkTmk = "UE68586760A163026C29710073AB2D7BE";
		String key = "U5F2DC42E10C92B16BA54802314CE95F5";
		String zmkTmkFlag = "1";
		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setKeyType("001")
				.setZmkTmkFlag(zmkTmkFlag)
				.setZmkTmk(zmkTmk)
				.setKey(key)
				.setKeySchemeZmkTmk("U")
				.build();

		byte[] body = command.packRequestBody(request);

		byte[] expected = ("001;" + zmkTmkFlag + zmkTmk + key + "U").getBytes(StandardCharsets.US_ASCII);
		assertArrayEquals(expected, body);
	}

	@Test
	void rejectsInvalidZmkTmkFlag() {
		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setKeyType("001")
				.setZmkTmkFlag("2")
				.setZmkTmk("UE68586760A163026C29710073AB2D7BE")
				.setKey("U5F2DC42E10C92B16BA54802314CE95F5")
				.setKeySchemeZmkTmk("U")
				.build();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.packRequestBody(request));
		assertEquals("ZMK/TMK flag must be '0' (ZMK) or '1' (TMK), got: 2", exception.getMessage());
	}

	@Test
	void rejectsMissingKeyType() {
		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setZmkTmkFlag("0")
				.setZmkTmk("UE68586760A163026C29710073AB2D7BE")
				.setKey("U5F2DC42E10C92B16BA54802314CE95F5")
				.setKeySchemeZmkTmk("U")
				.build();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.packRequestBody(request));
		assertEquals("A8 key type must be exactly 3 character(s)", exception.getMessage());
	}

	@Test
	void rejectsInvalidKeyTypeLength() {
		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setKeyType("01")
				.setZmkTmkFlag("0")
				.setZmkTmk("UE68586760A163026C29710073AB2D7BE")
				.setKey("U5F2DC42E10C92B16BA54802314CE95F5")
				.setKeySchemeZmkTmk("U")
				.build();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.packRequestBody(request));
		assertEquals("A8 key type must be exactly 3 character(s)", exception.getMessage());
	}

	@Test
	void rejectsMissingKeySchemeZmkTmk() {
		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setKeyType("001")
				.setZmkTmkFlag("0")
				.setZmkTmk("UE68586760A163026C29710073AB2D7BE")
				.setKey("U5F2DC42E10C92B16BA54802314CE95F5")
				.build();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.packRequestBody(request));
		assertEquals("A8 ZMK/TMK key scheme must be exactly 1 character(s)", exception.getMessage());
	}

	@Test
	void rejectsEmptyZmkTmk() {
		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setKeyType("001")
				.setZmkTmkFlag("0")
				.setZmkTmk("")
				.setKey("U5F2DC42E10C92B16BA54802314CE95F5")
				.setKeySchemeZmkTmk("U")
				.build();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.packRequestBody(request));
		assertEquals("A8 ZMK/TMK is required", exception.getMessage());
	}

	@Test
	void rejectsEmptyKey() {
		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setKeyType("001")
				.setZmkTmkFlag("0")
				.setZmkTmk("UE68586760A163026C29710073AB2D7BE")
				.setKey("")
				.setKeySchemeZmkTmk("U")
				.build();

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.packRequestBody(request));
		assertEquals("A8 key is required", exception.getMessage());
	}

	@Test
	void unpacksResponseBody() {
		String keyUnderZmkTmk = "XAC4D3C5F603C1B502E5F45668A155C25";
		String keyCheckValue = "AFDA4F";
		byte[] body = (keyUnderZmkTmk + keyCheckValue).getBytes(StandardCharsets.US_ASCII);

		ExportKeyResponse response = command.unpackResponseBody("00","1", body);

		assertEquals("00", response.getErrorCode());
		assertEquals(keyUnderZmkTmk, response.getKeyUnderZmkTmk());
		assertEquals(keyCheckValue, response.getKeyCheckValue());
	}

	@Test
	void returnsOnlyErrorCodeForEmptyBody() {
		ExportKeyResponse response = command.unpackResponseBody("10","1", new byte[0]);

		assertEquals("10", response.getErrorCode());
		assertEquals("", response.getKeyUnderZmkTmk());
		assertEquals("", response.getKeyCheckValue());
	}

	@Test
	void rejectsShortResponseBody() {
		byte[] body = "X1234".getBytes(StandardCharsets.US_ASCII);

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> command.unpackResponseBody("00","1", body));
		assertEquals(
				"A9 response body is too short: expected at least KCV, got 5 chars",
				exception.getMessage());
	}
}
