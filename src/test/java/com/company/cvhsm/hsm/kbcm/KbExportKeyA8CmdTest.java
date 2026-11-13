package com.company.cvhsm.hsm.kbcm;

import com.company.cvhsm.grpc.ExportKeyRequest;
import com.company.cvhsm.grpc.ExportKeyResponse;
import com.company.cvhsm.hsm.varcm.VrExportKeyA8Cmd;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThatIndexOutOfBoundsException;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KbExportKeyA8CmdTest {

	private final KbExportKeyA8Cmd command = new KbExportKeyA8Cmd();

	@Test
	void returnsCommandAndResponseCodes() {
		assertEquals("A8", command.getCommandCode());
		assertEquals("A9", command.getResponseCode());
	}

	@Test
	void packsBaseRequestBodyWithZmkFlag() {
		String zmkTmk = "S1009652TB00E0000737C4581F03E8D5DA741F4418EDD8D5FC3D423668E8C0234C889780C42CA651E56F9777849FF1416";
		String key = "S1009672TB00S00001997BF2B71360225A7575DCE3FC6468636795B9376356D1DD075FD49B0EA78BA81975F1A6F17E942";

		ExportKeyRequest request = ExportKeyRequest.newBuilder()
				.setKeyType("FFF")
				.setZmkTmk(zmkTmk)
				.setKey(key)
				.setKeySchemeZmkTmk("X")
				.build();

		byte[] body = command.packRequestBody(request);
		
		byte[] expected = ("FFF"  + zmkTmk + key + "X").getBytes(StandardCharsets.US_ASCII);

		assertArrayEquals(expected, body);
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
