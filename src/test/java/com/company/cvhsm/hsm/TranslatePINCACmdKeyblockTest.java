package com.company.cvhsm.hsm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.company.cvhsm.grpc.TranslatePinFromTpkToZpkRequest;
import com.company.cvhsm.grpc.TranslatePinFromTpkToZpkResponse;

class TranslatePINCACmdKeyblockTest {

    private final TranslatePINCACmd command = new TranslatePINCACmd("keyblock");

    public static final String SOURCE_ZPK = "S1009652TB00E0000737C4581F03E8D5DA741F4418EDD8D5FC3D423668E8C0234C889780C42CA651E56F9777849FF1416";
    public static final String DEST_ZPK = "S1009652TB00E0000737C4581F03E8D5DA741F4418EDD8D5FC3D423668E8C0234C889780C42CA651E56F9777849FF1416";
    @Test
    void returnsCommandAndResponseCodes() {
        assertEquals("CA", command.getCommandCode());
        assertEquals("CB", command.getResponseCode());
    }

    @Test
    void packsRequestBodyWithUSchemeKeys() {
        String minPinLength = "04";
        String sourcePinBlock = "1234567890ABCDEF";
        String sourceFormat = "01";
        String destinationFormat = "02";
        String panToken = "123456789012";

        TranslatePinFromTpkToZpkRequest request = TranslatePinFromTpkToZpkRequest.newBuilder()
                .setSourceTpk(SOURCE_ZPK)
                .setDestinationZpk(DEST_ZPK)
                .setMinimumPinLength(minPinLength)
                .setSourcePinBlock(sourcePinBlock)
                .setSourcePinBlockFormat(sourceFormat)
                .setDestinationPinBlockFormat(destinationFormat)
                .setPanToken(panToken)
                .build();

        byte[] body = command.packRequestBody(request);
        byte[] expected = (SOURCE_ZPK + DEST_ZPK + minPinLength + sourcePinBlock + sourceFormat + destinationFormat + panToken)
                .getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expected, body);
    }

    @Test
    void packsRequestBodyWithPlainKeys() {
        String minPinLength = "12";
        String sourcePinBlock = "1234567890ABCDEF";
        String sourceFormat = "47";
        String destinationFormat = "05";
        String panToken = "9876543210";

        TranslatePinFromTpkToZpkRequest request = TranslatePinFromTpkToZpkRequest.newBuilder()
                .setSourceTpk(SOURCE_ZPK)
                .setDestinationZpk(DEST_ZPK)
                .setMinimumPinLength(minPinLength)
                .setSourcePinBlock(sourcePinBlock)
                .setSourcePinBlockFormat(sourceFormat)
                .setDestinationPinBlockFormat(destinationFormat)
                .setPanToken(panToken)
                .build();

        byte[] body = command.packRequestBody(request);
        byte[] expected = (SOURCE_ZPK + DEST_ZPK + minPinLength + sourcePinBlock + sourceFormat + destinationFormat + panToken)
                .getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expected, body);
    }

    @Test
    void rejectsMinimumPinLengthBelowRange() {
        TranslatePinFromTpkToZpkRequest request = TranslatePinFromTpkToZpkRequest.newBuilder()
                .setSourceTpk(SOURCE_ZPK)
                .setDestinationZpk(DEST_ZPK)
                .setMinimumPinLength("03")
                .setSourcePinBlock("1234567890ABCDEF")
                .setSourcePinBlockFormat("01")
                .setDestinationPinBlockFormat("02")
                .setPanToken("123456789012")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("CA minium PIN length value must be between 4 and 12", exception.getMessage());
    }

    @Test
    void rejectsMinimumPinLengthAboveRange() {
        TranslatePinFromTpkToZpkRequest request = TranslatePinFromTpkToZpkRequest.newBuilder()
                .setSourceTpk(SOURCE_ZPK)
                .setDestinationZpk(DEST_ZPK)
                .setMinimumPinLength("13")
                .setSourcePinBlock("1234567890ABCDEF")
                .setSourcePinBlockFormat("01")
                .setDestinationPinBlockFormat("02")
                .setPanToken("123456789012")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("CA minium PIN length value must be between 4 and 12", exception.getMessage());
    }

    @Test
    void rejectsNonNumericMinimumPinLength() {
        TranslatePinFromTpkToZpkRequest request = TranslatePinFromTpkToZpkRequest.newBuilder()
                .setSourceTpk(SOURCE_ZPK)
                .setDestinationZpk(DEST_ZPK)
                .setMinimumPinLength("AB")
                .setSourcePinBlock("1234567890ABCDEF")
                .setSourcePinBlockFormat("01")
                .setDestinationPinBlockFormat("02")
                .setPanToken("123456789012")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("CA minium PIN length must be in numeric", exception.getMessage());
    }

    @Test
    void rejectsInvalidSourcePinBlockLength() {
        TranslatePinFromTpkToZpkRequest request = TranslatePinFromTpkToZpkRequest.newBuilder()
                .setSourceTpk(SOURCE_ZPK)
                .setDestinationZpk(DEST_ZPK)
                .setMinimumPinLength("04")
                .setSourcePinBlockFormat("01")
                .setDestinationPinBlockFormat("02")
                .setPanToken("123456789012")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("CA source PIN block is required", exception.getMessage());
    }

    

   
    @Test
    void rejectsEmptyPanToken() {
        TranslatePinFromTpkToZpkRequest request = TranslatePinFromTpkToZpkRequest.newBuilder()
                .setSourceTpk(SOURCE_ZPK)
                .setDestinationZpk(DEST_ZPK)
                .setMinimumPinLength("04")
                .setSourcePinBlock("1234567890ABCDEF")
                .setSourcePinBlockFormat("01")
                .setDestinationPinBlockFormat("02")
                .setPanToken("")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.packRequestBody(request));
        assertEquals("CA PAN/token is required", exception.getMessage());
    }

   


    @Test
    void unpacksResponseBodyWith16CharDestinationBlock() {
        String pinLength = "08";
        String destinationPinBlock = "1234567890ABCDEF";
        String sourcePinBlockFormat = "01";
        byte[] body = (pinLength + destinationPinBlock + sourcePinBlockFormat).getBytes(StandardCharsets.US_ASCII);

        TranslatePinFromTpkToZpkResponse response = command.unpackResponseBody("00","1", body);

        assertEquals("00", response.getErrorCode());
        assertEquals(pinLength, response.getPinLength());
        assertEquals(destinationPinBlock, response.getDestinationPinBlock());
        assertEquals(sourcePinBlockFormat, response.getSourcePinBlockFormat());
    }

    @Test
    void unpacksResponseBodyWith32CharDestinationBlock() {
        String pinLength = "12";
        String destinationPinBlock = "1234567890ABCDEF1234567890ABCDEF";
        String sourcePinBlockFormat = "47";
        byte[] body = (pinLength + destinationPinBlock + sourcePinBlockFormat).getBytes(StandardCharsets.US_ASCII);

        TranslatePinFromTpkToZpkResponse response = command.unpackResponseBody("00","1", body);

        assertEquals("00", response.getErrorCode());
        assertEquals(pinLength, response.getPinLength());
        assertEquals(destinationPinBlock, response.getDestinationPinBlock());
        assertEquals(sourcePinBlockFormat, response.getSourcePinBlockFormat());
    }

    @Test
    void returnsOnlyErrorCodeForEmptyBody() {
        TranslatePinFromTpkToZpkResponse response = command.unpackResponseBody("10","1", new byte[0]);

        assertEquals("10", response.getErrorCode());
        assertEquals("", response.getPinLength());
        assertEquals("", response.getDestinationPinBlock());
        assertEquals("", response.getSourcePinBlockFormat());
    }

    @Test
    void rejectsShortResponseBody() {
        byte[] body = "123".getBytes(StandardCharsets.US_ASCII);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> command.unpackResponseBody("00", "1",body));
        assertEquals(
                "CB response body is too short: expected at least 4 chars, got 3",
                exception.getMessage());
    }
}
