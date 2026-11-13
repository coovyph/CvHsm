package com.company.cvhsm.netty;

import com.company.cvhsm.hsm.HsmResponse;
import com.company.cvhsm.hsm.ResponseRouter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;

/**
 * Inbound handler that parses a deframed HSM response and dispatches it to the {@link ResponseRouter}.
 *
 * <p>The response format is: 4-char header + 2-char response code + 2-char error code + body.
 */
@ChannelHandler.Sharable
@Log4j2
@RequiredArgsConstructor
public class HsmChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final int HEADER_LENGTH = 4;
    private static final int RESPONSE_CODE_LENGTH = 2;
    private static final int ERROR_CODE_LENGTH = 2;
    private static final int MIN_RESPONSE_LENGTH = HEADER_LENGTH + RESPONSE_CODE_LENGTH + ERROR_CODE_LENGTH;

    private final ResponseRouter responseRouter;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        if (msg.readableBytes() < MIN_RESPONSE_LENGTH) {
            log.warn("Received HSM response shorter than minimum length: {} bytes", msg.readableBytes());
            return;
        }

        String header = msg.readSlice(HEADER_LENGTH).toString(StandardCharsets.US_ASCII);
        String responseCode = msg.readSlice(RESPONSE_CODE_LENGTH).toString(StandardCharsets.US_ASCII);
        String errorCode = msg.readSlice(ERROR_CODE_LENGTH).toString(StandardCharsets.US_ASCII);

        byte[] body = new byte[msg.readableBytes()];
        msg.readBytes(body);

        HsmResponse response = HsmResponse.builder()
                .header(header)
                .responseCode(responseCode)
                .errorCode(errorCode)
                .body(body)
                .build();
        

        log.debug("Received HSM response header={}, responseCode={}, errorCode={}",
                header, responseCode, errorCode);

        responseRouter.complete(response);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("HSM channel {} became inactive", ctx.channel().id());
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("HSM channel {} encountered an exception", ctx.channel().id(), cause);
        ctx.close();
    }
}
