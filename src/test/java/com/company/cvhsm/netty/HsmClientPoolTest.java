package com.company.cvhsm.netty;

import com.company.cvhsm.config.HsmProperties;
import com.company.cvhsm.hsm.HsmRequest;
import com.company.cvhsm.hsm.HsmResponse;
import com.company.cvhsm.hsm.ResponseRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HsmClientPoolTest {

    private static final int MAX_FRAME_LENGTH = 65535;
    private static final int LENGTH_FIELD_LENGTH = 2;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int BYTES_TO_STRIP = 2;

    private EventLoopGroup serverGroup;
    private Channel serverChannel;
    private int serverPort;

    private HsmProperties hsmProperties;
    private ResponseRouter responseRouter;
    private HsmChannelPool channelPool;
    private HsmClient hsmClient;

    @BeforeEach
    void setUp() throws Exception {
        serverGroup = new NioEventLoopGroup(1);
        serverChannel = startFakeHsmServer(serverGroup);
        serverPort = ((java.net.InetSocketAddress) serverChannel.localAddress()).getPort();

        hsmProperties = createProperties(serverPort);
        responseRouter = new ResponseRouter();
        channelPool = new HsmChannelPool(hsmProperties, responseRouter);
        channelPool.start();
        hsmClient = new HsmClient(channelPool, responseRouter);

        waitForChannels(2);
    }

    @AfterEach
    void tearDown() {
        if (channelPool != null) {
            channelPool.stop();
        }
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (serverGroup != null) {
            serverGroup.shutdownGracefully();
        }
    }

    @Test
    void eagerlyEstablishesConfiguredSessions() {
        assertEquals(2, channelPool.activeChannelCount());
    }

    @Test
    void sendsRequestAndReceivesCorrelatedResponse() throws Exception {
        byte[] body = "hello".getBytes(StandardCharsets.US_ASCII);
        HsmRequest request = HsmRequest.builder()
                .header("ABCD")
                .commandCode("B2")
                .body(body)
                .build();

        CompletableFuture<HsmResponse> future = hsmClient.send(request, hsmProperties.getRequestTimeoutMillis());

        HsmResponse response = future.get(5, TimeUnit.SECONDS);
        assertEquals("ABCD", response.getHeader());
        assertEquals("B3", response.getResponseCode());
        assertEquals("00", response.getErrorCode());
        assertArrayEquals(body, response.getBody());
    }

    @Test
    void throwsWhenNoChannelsAreAvailable() {
        serverChannel.close();
        channelPool.stop();

        HsmRequest request = HsmRequest.builder()
                .header("WXYZ")
                .commandCode("B2")
                .body(new byte[0])
                .build();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> hsmClient.send(request, hsmProperties.getRequestTimeoutMillis()));
        assertTrue(exception.getMessage().contains("No active HSM channels"));
    }

    private Channel startFakeHsmServer(EventLoopGroup group) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(
                                        MAX_FRAME_LENGTH,
                                        LENGTH_FIELD_OFFSET,
                                        LENGTH_FIELD_LENGTH,
                                        LENGTH_ADJUSTMENT,
                                        BYTES_TO_STRIP))
                                .addLast(new LengthFieldPrepender(LENGTH_FIELD_LENGTH, false))
                                .addLast(new FakeHsmHandler());
                    }
                });

        ChannelFuture future = bootstrap.bind(0).sync();
        return future.channel();
    }

    private HsmProperties createProperties(int port) {
        HsmProperties properties = new HsmProperties();
        properties.setHost("localhost");
        properties.setPort(port);
        properties.setMaxSessions(2);
        properties.setConnectTimeoutMillis(5000);
        properties.setRequestTimeoutMillis(5000);
        properties.setReconnectDelayMillis(500);
        return properties;
    }

    private void waitForChannels(int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (channelPool.activeChannelCount() < expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }
    }

    private static class FakeHsmHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf request = (ByteBuf) msg;
            try {
                String header = request.readSlice(4).toString(StandardCharsets.US_ASCII);
                String commandCode = request.readSlice(2).toString(StandardCharsets.US_ASCII);
                byte[] body = new byte[request.readableBytes()];
                request.readBytes(body);

                String responseCode = "B2".equals(commandCode) ? "B3" : "00";
                ByteBuf response = Unpooled.buffer(4 + 2 + 2 + body.length);
                response.writeBytes(header.getBytes(StandardCharsets.US_ASCII));
                response.writeBytes(responseCode.getBytes(StandardCharsets.US_ASCII));
                response.writeBytes("00".getBytes(StandardCharsets.US_ASCII));
                response.writeBytes(body);

                ctx.writeAndFlush(response);
            } finally {
                request.release();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}
