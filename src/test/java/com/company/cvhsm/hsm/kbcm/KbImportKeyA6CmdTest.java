package com.company.cvhsm.hsm.kbcm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.company.cvhsm.grpc.ImportKeyRequest;
import com.company.cvhsm.grpc.ImportKeyResponse;
import com.company.cvhsm.hsm.varcm.VrImportKeyA6Cmd;

class KbImportKeyA6CmdTest {

    private final KbImportKeyA6Cmd command = new KbImportKeyA6Cmd();

    @Test
    void returnsCommandAndResponseCodes() {
        assertEquals("A6", command.getCommandCode());
        assertEquals("A7", command.getResponseCode());
    }

    @Test
    void packsBaseRequestBody() {
        String zmk = "S1009652TB00E0000737C4581F03E8D5DA741F4418EDD8D5FC3D423668E8C0234C889780C42CA651E56F9777849FF1416";
        String keyUnderZmk = "XAC4D3C5F603C1B502E5F45668A155C25";
        ImportKeyRequest request = ImportKeyRequest.newBuilder()
                .setKeyType("FFF")
                .setZmk(zmk)
                .setKeyUnderZmk(keyUnderZmk)
                .setKeySchemeLmk("S")
                .setKeyUsage("72")
                .setModeOfUse("N")
                .setKeyVersionNumber("99")
                .setExportability("S")
                .build();

        byte[] body = command.packRequestBody(request);
        

        byte[] expected = ("FFF" + zmk + keyUnderZmk + "S#72N99S00").getBytes(StandardCharsets.US_ASCII);
        assertArrayEquals(expected, body);
    }

    
    @Test
    void rejectsMissingKeyType() {
        ImportKeyRequest request = ImportKeyRequest.newBuilder()
                .setZmk("UE68586760A163026C29710073AB2D7BE")
                .setKeyUnderZmk("XAC4D3C5F603C1B502E5F45668A155C25")
                .setKeySchemeLmk("U")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("A6 key type must be exactly 3 character(s)", exception.getMessage());
    }

    
    @Test
    void rejectsInvalidKeyTypeLength() {
        ImportKeyRequest request = ImportKeyRequest.newBuilder()
                .setKeyType("01")
                .setZmk("UE68586760A163026C29710073AB2D7BE")
                .setKeyUnderZmk("XAC4D3C5F603C1B502E5F45668A155C25")
                .setKeySchemeLmk("U")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("A6 key type must be exactly 3 character(s)", exception.getMessage());
    }

    
    @Test
    void rejectsMissingKeySchemeLmk() {
        ImportKeyRequest request = ImportKeyRequest.newBuilder()
                .setKeyType("001")
                .setZmk("UE68586760A163026C29710073AB2D7BE")
                .setKeyUnderZmk("XAC4D3C5F603C1B502E5F45668A155C25")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("A6 LMK key scheme must be exactly 1 character(s)", exception.getMessage());
    }

    
    @Test
    void rejectsEmptyZmk() {
        ImportKeyRequest request = ImportKeyRequest.newBuilder()
                .setKeyType("001")
                .setZmk("")
                .setKeyUnderZmk("XAC4D3C5F603C1B502E5F45668A155C25")
                .setKeySchemeLmk("U")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("A6 ZMK is required", exception.getMessage());
    }

    @Test
    void rejectsEmptyKeyUnderZmk() {
        ImportKeyRequest request = ImportKeyRequest.newBuilder()
                .setKeyType("001")
                .setZmk("UE68586760A163026C29710073AB2D7BE")
                .setKeyUnderZmk("")
                .setKeySchemeLmk("U")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("A6 key under ZMK is required", exception.getMessage());
    }

    @Test
    void unpacksResponseBody() {
        String keyUnderLmk = "U5F2DC42E10C92B16BA54802314CE95F5";
        String keyCheckValue = "AFDA4F";
        byte[] body = (keyUnderLmk + keyCheckValue).getBytes(StandardCharsets.US_ASCII);

        ImportKeyResponse response = command.unpackResponseBody("00","1", body);

        assertEquals("00", response.getErrorCode());
        assertEquals(keyUnderLmk, response.getKeyUnderLmk());
        assertEquals(keyCheckValue, response.getKeyCheckValue());
    }

    @Test
    void returnsOnlyErrorCodeForEmptyBody() {
        ImportKeyResponse response = command.unpackResponseBody("10","1", new byte[0]);

        assertEquals("10", response.getErrorCode());
        assertEquals("", response.getKeyUnderLmk());
        assertEquals("", response.getKeyCheckValue());
    }

    @Test
    void rejectsShortResponseBody() {
        byte[] body = "U1234".getBytes(StandardCharsets.US_ASCII);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.unpackResponseBody("00","1", body));
        assertEquals(
                "A7 response body is too short: expected at least KCV, got 5 chars",
                exception.getMessage());
    }
    
}
