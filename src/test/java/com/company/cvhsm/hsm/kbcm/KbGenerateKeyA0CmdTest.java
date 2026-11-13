package com.company.cvhsm.hsm.kbcm;

import com.company.cvhsm.grpc.GenerateKeyRequest;
import com.company.cvhsm.grpc.GenerateKeyResponse;
import com.company.cvhsm.hsm.varcm.VrGenerateKeyA0Cmd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KbGenerateKeyA0CmdTest {

	private final static String DUMMY_ZMK = "S1009622TN00S00006B8166B71F225E29CC933B50314B4C745F0F4C230D455DABA2E11A1A599E8154CBBA62527E0DE7A9";
    private final KbGenerateKeyA0Cmd command = new KbGenerateKeyA0Cmd();

    @Test
    void returnsCommandAndResponseCodes() {
        assertEquals("A0", command.getCommandCode());
        assertEquals("A1", command.getResponseCode());
    }

    @Test
    void packsMode1RequestBodyWithZmkFlag() {
        GenerateKeyRequest request = GenerateKeyRequest.newBuilder()
                .setMode("1")
                .setKeyType("FFF")
                .setKeySchemeLmk("S")
                .setZmkTmkFlag("0")
                .setZmk(DUMMY_ZMK)
                .setKeySchemeZmk("X")
                .setKeyUsage("72")
                .setAlgorithm("T2")
                .setModeOfUse("N")
                .setKeyVersionNumber("99")
                .setExportability("S")
                .build();

        byte[] expectedPrefix = "1FFFS;0".getBytes(StandardCharsets.US_ASCII);
        byte[] expectedZmk = DUMMY_ZMK.getBytes(StandardCharsets.US_ASCII);
        byte[] expectedSuffix = "X#72T2N99S00".getBytes(StandardCharsets.US_ASCII);

        byte[] body = command.packRequestBody(request);

        byte[] expected = new byte[expectedPrefix.length + expectedZmk.length + expectedSuffix.length];
        System.arraycopy(expectedPrefix, 0, expected, 0, expectedPrefix.length);
        System.arraycopy(expectedZmk, 0, expected, expectedPrefix.length, expectedZmk.length);
        System.arraycopy(expectedSuffix, 0, expected, expectedPrefix.length + expectedZmk.length, expectedSuffix.length);

        assertArrayEquals(expected, body);
    }

    @Test
    void packsMode1RequestBodyWithTmkFlag() {
        GenerateKeyRequest request = GenerateKeyRequest.newBuilder()
                .setMode("1")
                .setKeyType("FFF")
                .setKeySchemeLmk("S")
                .setZmkTmkFlag("1")
                .setZmk(DUMMY_ZMK)
                .setKeySchemeZmk("X")
                .setKeyUsage("72")
                .setAlgorithm("T2")
                .setModeOfUse("N")
                .setKeyVersionNumber("99")
                .setExportability("S")
                .build();

        byte[] expectedPrefix = "1FFFS;1".getBytes(StandardCharsets.US_ASCII);
        byte[] expectedZmk = DUMMY_ZMK.getBytes(StandardCharsets.US_ASCII);
        byte[] expectedSuffix = "X#72T2N99S00".getBytes(StandardCharsets.US_ASCII);

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
                .setKeyType("FFF")
                .setKeySchemeLmk("S")
                .setZmkTmkFlag("2")
                .setZmk(DUMMY_ZMK)
                .setKeySchemeZmk("X")
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
                .setZmk(DUMMY_ZMK)
                .setKeySchemeZmk("U")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("A0 mode value is expected one of 0/1", exception.getMessage());
    }

    @Test
    void rejectsMissingZmkForMode1() {
        GenerateKeyRequest request = GenerateKeyRequest.newBuilder()
                .setMode("1")
                .setKeyType("001")
                .setKeySchemeLmk("U")
                .setZmkTmkFlag("0")
                .setKeySchemeZmk("U")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        
          assertEquals("A0 key type value must be FFF", exception.getMessage());
    }

    @Test
    void unpacksResponseBody() {
        String keyUnderLmk = DUMMY_ZMK;
        String keyUnderZmk = "X" + "B".repeat(32);
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
