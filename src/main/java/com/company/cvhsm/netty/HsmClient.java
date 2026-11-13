package com.company.cvhsm.netty;

import com.company.cvhsm.hsm.HsmRequest;
import com.company.cvhsm.hsm.HsmResponse;
import com.company.cvhsm.hsm.ResponseRouter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * High-level TCP client facade for sending HSM requests and receiving correlated responses.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class HsmClient {

    private final HsmChannelPool channelPool;
    private final ResponseRouter responseRouter;

    /**
     * Sends a request to the HSM over a borrowed channel and returns a future that is completed
     * when the correlated response arrives or fails with a timeout if the configured timeout expires.
     *
     * @param request         the HSM request to send
     * @param timeoutMillis   the maximum time to wait for a response
     * @return a future that completes with the HSM response
     */
    public CompletableFuture<HsmResponse> send(HsmRequest request, long timeoutMillis) {
        return sendToChannel(channelPool.borrowChannel(), request, timeoutMillis);
    }

    /**
     * Sends a request to the HSM over a specific channel and returns a future that is completed
     * when the correlated response arrives or fails with a timeout if the configured timeout expires.
     *
     * @param channel         the channel to use for the request
     * @param request         the HSM request to send
     * @param timeoutMillis   the maximum time to wait for a response
     * @return a future that completes with the HSM response
     */
    public CompletableFuture<HsmResponse> sendToChannel(Channel channel, HsmRequest request, long timeoutMillis) {
        CompletableFuture<HsmResponse> responseFuture = responseRouter.register(request.getHeader(),request.getCache(), timeoutMillis);

        ByteBuf message = encodeMessage(request);

        channel.writeAndFlush(message).addListener(writeFuture -> {
            if (!writeFuture.isSuccess()) {
                responseRouter.fail(request.getHeader(), writeFuture.cause());
            }
        });

        log.debug("Sent HSM request header={}, commandCode={} on channel {}",
                request.getHeader(), request.getCommandCode(), channel.id());

        return responseFuture;
    }

    private static ByteBuf encodeMessage(HsmRequest request) {
        ByteBuf message = Unpooled.buffer(request.getMessageLength());
        message.writeBytes(request.getHeader().getBytes(StandardCharsets.US_ASCII));
        message.writeBytes(request.getCommandCode().getBytes(StandardCharsets.US_ASCII));
        message.writeBytes(request.getBody());
        return message;
    }
}
