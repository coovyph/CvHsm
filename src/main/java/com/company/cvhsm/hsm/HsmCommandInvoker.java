package com.company.cvhsm.hsm;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.company.cvhsm.config.HsmProperties;
import com.company.cvhsm.netty.HsmClient;

import lombok.RequiredArgsConstructor;

/**
 * Executes an HSM command by packing a gRPC request, sending it through the TCP client, and unpacking the response.
 *
 * <p>HSM error codes are passed to the command's {@code unpackResponseBody} implementation; each command decides
 * whether to surface the error as an exception or to return it as part of the gRPC response.
 */
@Component
@RequiredArgsConstructor
public class HsmCommandInvoker {

    private final HsmClient hsmClient;
    private final CorrelationIdGenerator correlationIdGenerator;
    private final HsmProperties hsmProperties;

    /**
     * Invokes the given HSM command with the configured request timeout.
     *
     * @param request the gRPC request
     * @param command the command implementation that handles packing and unpacking
     * @return a future that completes with the gRPC response
     */
    public <Req, Resp> CompletableFuture<Resp> invoke(Req request, HsmCommand<Req, Resp> command) {
        String header = correlationIdGenerator.generate();
        byte[] body = command.packRequestBody(request);
        Object cache = command.createCache(request);
        
        HsmRequest hsmRequest = HsmRequest.builder()
                .header(header)
                .commandCode(command.getCommandCode())
                .body(body)
                .cache(cache)
                .build();

        return hsmClient.send(hsmRequest, hsmProperties.getRequestTimeoutMillis())
                .thenApply(response -> {
                    return command.unpackResponseBody(response.getErrorCode(), response.getCache(), response.getBody());
                });
    }
    
}
