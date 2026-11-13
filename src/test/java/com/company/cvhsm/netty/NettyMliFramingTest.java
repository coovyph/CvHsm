package com.company.cvhsm.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the MLI framing implementation using Netty's built-in
 * {@link LengthFieldPrepender} and {@link LengthFieldBasedFrameDecoder}.
 *
 * <p>MLI format: 2 bytes big-endian length, value excludes the 2-byte header itself.
 */
class NettyMliFramingTest {

    private static final int MAX_FRAME_LENGTH = 65535;
    private static final int LENGTH_FIELD_LENGTH = 2;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int BYTES_TO_STRIP = 2;

    private EmbeddedChannel encoderChannel;
    private EmbeddedChannel decoderChannel;

    @BeforeEach
    void setUp() {
        encoderChannel = new EmbeddedChannel(new LengthFieldPrepender(LENGTH_FIELD_LENGTH, false));
        decoderChannel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(
                        MAX_FRAME_LENGTH,
                        LENGTH_FIELD_OFFSET,
                        LENGTH_FIELD_LENGTH,
                        LENGTH_ADJUSTMENT,
                        BYTES_TO_STRIP));
    }

    @AfterEach
    void tearDown() {
        encoderChannel.close();
        decoderChannel.close();
    }

    @Test
    void encoderPrefixesMessageWithTwoByteBigEndianLength() {
        byte[] payload = "HELLO".getBytes(StandardCharsets.US_ASCII);
        ByteBuf input = Unpooled.wrappedBuffer(payload);

        assertTrue(encoderChannel.writeOutbound(input));
        assertTrue(encoderChannel.finish());

        ByteBuf header = encoderChannel.readOutbound();
        assertNotNull(header);
        assertEquals(LENGTH_FIELD_LENGTH, header.readableBytes());
        assertEquals(payload.length, header.readUnsignedShort());
        header.release();

        ByteBuf body = encoderChannel.readOutbound();
        assertNotNull(body);
        assertEquals(payload.length, body.readableBytes());

        byte[] decodedPayload = new byte[payload.length];
        body.readBytes(decodedPayload);
        assertArrayEquals(payload, decodedPayload);
        body.release();

        assertNull(encoderChannel.readOutbound());
    }

    @Test
    void decoderStripsMliHeaderAndReturnsBody() {
        byte[] payload = "WORLD".getBytes(StandardCharsets.US_ASCII);
        ByteBuf input = Unpooled.buffer(2 + payload.length);
        input.writeShort(payload.length);
        input.writeBytes(payload);

        assertTrue(decoderChannel.writeInbound(input));

        ByteBuf output = decoderChannel.readInbound();
        assertNotNull(output);
        assertEquals(payload.length, output.readableBytes());

        byte[] decodedPayload = new byte[payload.length];
        output.readBytes(decodedPayload);
        assertArrayEquals(payload, decodedPayload);
        output.release();
    }

    @Test
    void decoderWaitsForFullFrameBeforeEmitting() {
        byte[] payload = "PARTIAL".getBytes(StandardCharsets.US_ASCII);
        ByteBuf firstPart = Unpooled.buffer();
        firstPart.writeShort(payload.length);
        firstPart.writeBytes(payload, 0, 3);

        decoderChannel.writeInbound(firstPart);
        assertNull(decoderChannel.readInbound());

        ByteBuf secondPart = Unpooled.wrappedBuffer(payload, 3, payload.length - 3);
        decoderChannel.writeInbound(secondPart);

        ByteBuf output = decoderChannel.readInbound();
        assertNotNull(output);
        assertEquals(payload.length, output.readableBytes());

        byte[] decodedPayload = new byte[payload.length];
        output.readBytes(decodedPayload);
        assertArrayEquals(payload, decodedPayload);
        output.release();
    }

    @Test
    void decoderHandlesMultipleFramesInOneBuffer() {
        byte[] payload1 = "ONE".getBytes(StandardCharsets.US_ASCII);
        byte[] payload2 = "TWO".getBytes(StandardCharsets.US_ASCII);

        ByteBuf input = Unpooled.buffer();
        input.writeShort(payload1.length);
        input.writeBytes(payload1);
        input.writeShort(payload2.length);
        input.writeBytes(payload2);

        assertTrue(decoderChannel.writeInbound(input));

        ByteBuf output1 = decoderChannel.readInbound();
        ByteBuf output2 = decoderChannel.readInbound();
        assertNotNull(output1);
        assertNotNull(output2);

        byte[] decoded1 = new byte[payload1.length];
        output1.readBytes(decoded1);
        assertArrayEquals(payload1, decoded1);
        output1.release();

        byte[] decoded2 = new byte[payload2.length];
        output2.readBytes(decoded2);
        assertArrayEquals(payload2, decoded2);
        output2.release();

        assertNull(decoderChannel.readInbound());
    }

    @Test
    void encoderRejectsMessageExceedingMaxMliLength() {
        byte[] oversized = new byte[MAX_FRAME_LENGTH + 1];
        ByteBuf input = Unpooled.wrappedBuffer(oversized);

        assertThrows(EncoderException.class, () -> encoderChannel.writeOutbound(input));
        assertFalse(encoderChannel.finish());
    }
}
