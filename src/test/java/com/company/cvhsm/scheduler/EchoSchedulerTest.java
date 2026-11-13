package com.company.cvhsm.scheduler;

import com.company.cvhsm.config.HsmProperties;
import com.company.cvhsm.hsm.CorrelationIdGenerator;
import com.company.cvhsm.hsm.HsmRequest;
import com.company.cvhsm.hsm.HsmResponse;
import com.company.cvhsm.hsm.ResponseRouter;
import com.company.cvhsm.netty.HsmChannelPool;
import com.company.cvhsm.netty.HsmClient;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EchoSchedulerTest {

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^[0-9]{14}$");

    private CorrelationIdGenerator correlationIdGenerator;
    private HsmProperties hsmProperties;
    private HsmChannelPool channelPool;
    private HsmClient hsmClient;
    private EchoScheduler scheduler;
    private ResponseRouter responseRouter;

    @BeforeEach
    void setUp() {
        correlationIdGenerator = new CorrelationIdGenerator();

        hsmProperties = new HsmProperties();
        hsmProperties.getEcho().setEnabled(true);
        hsmProperties.getEcho().setIntervalMillis(30000);

        channelPool = mock(HsmChannelPool.class);
        hsmClient = mock(HsmClient.class);
        responseRouter = mock(ResponseRouter.class);

        scheduler = new EchoScheduler(hsmProperties, channelPool, hsmClient, correlationIdGenerator, responseRouter);
    }

    @Test
    void sendsEchoToAllActiveChannels() {
        Channel channel1 = mock(Channel.class);
        Channel channel2 = mock(Channel.class);
        when(channel1.isActive()).thenReturn(true);
        when(channel2.isActive()).thenReturn(true);
        when(channelPool.activeChannels()).thenReturn(List.of(channel1, channel2));

        when(hsmClient.sendToChannel(
                org.mockito.ArgumentMatchers.any(Channel.class),
                org.mockito.ArgumentMatchers.any(HsmRequest.class),
                org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(HsmResponse.builder()
                        .header("ABCD")
                        .responseCode("B3")
                        .errorCode("00")
                        .body(new byte[0])
                        .build()));

        scheduler.sendEchoToAllChannels();

        verify(hsmClient).sendToChannel(org.mockito.ArgumentMatchers.eq(channel1), org.mockito.ArgumentMatchers.any(HsmRequest.class), org.mockito.ArgumentMatchers.eq(5000L));
        verify(hsmClient).sendToChannel(org.mockito.ArgumentMatchers.eq(channel2), org.mockito.ArgumentMatchers.any(HsmRequest.class), org.mockito.ArgumentMatchers.eq(5000L));
    }

    @Test
    void echoBodyContainsFourHexLengthAndTimestamp() {
        Channel channel = mock(Channel.class);
        when(channel.isActive()).thenReturn(true);
        when(channelPool.activeChannels()).thenReturn(List.of(channel));

        ArgumentCaptor<HsmRequest> requestCaptor = ArgumentCaptor.forClass(HsmRequest.class);
        when(hsmClient.sendToChannel(
                org.mockito.ArgumentMatchers.eq(channel),
                requestCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(5000L)))
                .thenReturn(CompletableFuture.completedFuture(HsmResponse.builder()
                        .header("ABCD")
                        .responseCode("B3")
                        .errorCode("00")
                        .body(new byte[0])
                        .build()));

        scheduler.sendEchoToAllChannels();

        HsmRequest request = requestCaptor.getValue();
        assertEquals("B2", request.getCommandCode());
        assertEquals(4, request.getHeader().length());

        String body = new String(request.getBody(), StandardCharsets.US_ASCII);
        assertEquals(18, body.length());
        assertEquals("000E", body.substring(0, 4));
        String timestamp = body.substring(4);
        assertTrue(TIMESTAMP_PATTERN.matcher(timestamp).matches(), "Timestamp should be 14 digits: " + timestamp);
    }

    @Test
    void skipsInactiveChannels() {
        Channel activeChannel = mock(Channel.class);
        Channel inactiveChannel = mock(Channel.class);
        when(activeChannel.isActive()).thenReturn(true);
        when(inactiveChannel.isActive()).thenReturn(false);
        when(channelPool.activeChannels()).thenReturn(List.of(activeChannel, inactiveChannel));

        when(hsmClient.sendToChannel(
                org.mockito.ArgumentMatchers.any(Channel.class),
                org.mockito.ArgumentMatchers.any(HsmRequest.class),
                org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(CompletableFuture.completedFuture(HsmResponse.builder()
                        .header("ABCD")
                        .responseCode("B3")
                        .errorCode("00")
                        .body(new byte[0])
                        .build()));

        scheduler.sendEchoToAllChannels();

        verify(hsmClient).sendToChannel(org.mockito.ArgumentMatchers.eq(activeChannel), org.mockito.ArgumentMatchers.any(HsmRequest.class), org.mockito.ArgumentMatchers.eq(5000L));
    }

    @Test
    void doesNothingWhenEchoIsDisabled() {
        hsmProperties.getEcho().setEnabled(false);
        when(channelPool.activeChannels()).thenReturn(List.of(mock(Channel.class)));

        scheduler.sendEchoToAllChannels();

        org.mockito.Mockito.verifyNoInteractions(hsmClient);
    }
}
