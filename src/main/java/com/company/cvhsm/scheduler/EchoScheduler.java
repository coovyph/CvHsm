package com.company.cvhsm.scheduler;

import com.company.cvhsm.config.HsmProperties;
import com.company.cvhsm.hsm.CorrelationIdGenerator;
import com.company.cvhsm.hsm.HsmRequest;
import com.company.cvhsm.hsm.HsmResponse;
import com.company.cvhsm.hsm.ResponseRouter;
import com.company.cvhsm.netty.HsmChannelPool;
import com.company.cvhsm.netty.HsmClient;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Periodically sends an echo command (B2) to every active HSM session to keep the connections alive.
 *
 * <p>The echo body is built as: 4-hex length of the data field followed by the date/time in
 * {@code YYYYMMDDHHMMSS} format.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class EchoScheduler {

    private static final String ECHO_COMMAND_CODE = "B2";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final long ECHO_TIMEOUT_MILLIS = 5000;

    private final HsmProperties hsmProperties;
    private final HsmChannelPool channelPool;
    private final HsmClient hsmClient;
    private final CorrelationIdGenerator correlationIdGenerator;
    private final ResponseRouter responseRouter;

    @Scheduled(fixedDelayString = "${hsm.echo.interval-millis}", initialDelayString = "${hsm.echo.interval-millis}", timeUnit = TimeUnit.MILLISECONDS)
    public void sendEchoToAllChannels() {
        if (!hsmProperties.getEcho().isEnabled()) {
            return;
        }

        HsmRequest echoRequest = null;
        int channelCount = 0;

        for (Channel channel : channelPool.activeChannels()) {
            if (!channel.isActive()) {
                log.warn("Skipping echo for inactive HSM channel {}", channel.id());
                continue;
            }

            channelCount++;
            echoRequest = buildEchoRequest();
            responseRouter.register(echoRequest.getHeader(), ECHO_TIMEOUT_MILLIS);
            hsmClient.sendToChannel(channel, echoRequest, ECHO_TIMEOUT_MILLIS)
                    .whenComplete((response, throwable) -> onEchoResponse(channel, response, throwable));
        }

        log.debug("Sent echo request to {} active HSM channel(s)", channelCount);
    }

    private HsmRequest buildEchoRequest() {
        String data = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        String lengthHex = String.format("%04X", data.length());
        byte[] body = (lengthHex + data).getBytes(StandardCharsets.US_ASCII);

        return HsmRequest.builder()
                .header(correlationIdGenerator.generate())
                .commandCode(ECHO_COMMAND_CODE)
                .body(body)
                .build();
    }

    private void onEchoResponse(Channel channel, HsmResponse response, Throwable throwable) {
        if (throwable != null) {
            log.warn("Echo request failed on HSM channel {}", channel.id(), throwable);
            return;
        }

        if (!"00".equals(response.getErrorCode())) {
            log.warn("Echo request returned error code {} on HSM channel {}",
                    response.getErrorCode(), channel.id());
            return;
        }

        log.debug("Echo request succeeded on HSM channel {}", channel.id());
    }
}
