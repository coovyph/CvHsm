package com.company.cvhsm.netty;

import com.company.cvhsm.hsm.ResponseRouter;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Initializes a Netty TCP channel for the Thales HSM protocol.
 *
 * <p>
 * Pipeline:
 * <ol>
 * <li>{@link LengthFieldPrepender} — adds 2-byte big-endian MLI header (length
 * excludes header).</li>
 * <li>{@link LengthFieldBasedFrameDecoder} — reads 2-byte MLI header and
 * buffers full HSM message.</li>
 * <li>{@link HsmChannelHandler} — parses responses and dispatches them to the
 * response router.</li>
 * </ol>
 */
public class HsmChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final int MAX_FRAME_LENGTH = 65535;
    private static final int LENGTH_FIELD_LENGTH = 2;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int BYTES_TO_STRIP = 2;

    private final ResponseRouter responseRouter;

    public HsmChannelInitializer(ResponseRouter responseRouter) {
        this.responseRouter = responseRouter;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new LoggingHandler(LogLevel.INFO))
                .addLast("mliEncoder", new LengthFieldPrepender(LENGTH_FIELD_LENGTH, false))
                .addLast("mliDecoder", new LengthFieldBasedFrameDecoder(
                        MAX_FRAME_LENGTH,
                        LENGTH_FIELD_OFFSET,
                        LENGTH_FIELD_LENGTH,
                        LENGTH_ADJUSTMENT,
                        BYTES_TO_STRIP))
                .addLast("hsmHandler", new HsmChannelHandler(responseRouter));
    }
}
