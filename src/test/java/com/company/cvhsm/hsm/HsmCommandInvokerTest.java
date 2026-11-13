package com.company.cvhsm.hsm;

import com.company.cvhsm.config.HsmProperties;
import com.company.cvhsm.netty.HsmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HsmCommandInvokerTest {

    private CorrelationIdGenerator correlationIdGenerator;
    private HsmProperties hsmProperties;

    @BeforeEach
    void setUp() {
        correlationIdGenerator = new CorrelationIdGenerator();
        hsmProperties = new HsmProperties();
        hsmProperties.setRequestTimeoutMillis(5000);
    }

    @Test
    void invokesCommandAndUnpacksResponse() throws Exception {
        HsmCommand<String, String> command = new TestCommand(
                "B2", "B3", body -> new String(body, StandardCharsets.US_ASCII));

        HsmClient fakeClient = new HsmClient(null, null) {
            @Override
            public CompletableFuture<HsmResponse> send(HsmRequest request, long timeoutMillis) {
                return CompletableFuture.completedFuture(HsmResponse.builder()
                        .header(request.getHeader())
                        .responseCode(command.getResponseCode())
                        .errorCode("00")
                        .body(request.getBody())
                        .build());
            }
        };

        HsmCommandInvoker invoker = new HsmCommandInvoker(fakeClient, correlationIdGenerator, hsmProperties);

        CompletableFuture<String> result = invoker.invoke("hello", command);
        assertEquals("hello", result.get(1, TimeUnit.SECONDS));
    }

    @Test
    void passesHsmErrorCodeToCommandWithoutException() throws Exception {
        HsmCommand<String, String> command = new TestCommand(
                "B2", "B3", body -> new String(body, StandardCharsets.US_ASCII));

        HsmClient fakeClient = new HsmClient(null, null) {
            @Override
            public CompletableFuture<HsmResponse> send(HsmRequest request, long timeoutMillis) {
                return CompletableFuture.completedFuture(HsmResponse.builder()
                        .header(request.getHeader())
                        .responseCode(command.getResponseCode())
                        .errorCode("10")
                        .body("error-body".getBytes(StandardCharsets.US_ASCII))
                        .build());
            }
        };

        HsmCommandInvoker invoker = new HsmCommandInvoker(fakeClient, correlationIdGenerator, hsmProperties);

        assertEquals("error-body", invoker.invoke("hello", command).get(1, TimeUnit.SECONDS));
    }

    private static final class TestCommand implements HsmCommand<String, String> {

        private final String commandCode;
        private final String responseCode;
        private final java.util.function.Function<byte[], String> unpacker;

        TestCommand(String commandCode, String responseCode, java.util.function.Function<byte[], String> unpacker) {
            this.commandCode = commandCode;
            this.responseCode = responseCode;
            this.unpacker = unpacker;
        }

        @Override
        public String getCommandCode() {
            return commandCode;
        }

        @Override
        public String getResponseCode() {
            return responseCode;
        }

        @Override
        public byte[] packRequestBody(String request) {
            return request.getBytes(StandardCharsets.US_ASCII);
        }

        @Override
        public String unpackResponseBody(String errorCode,Object cache, byte[] body) {
            return unpacker.apply(body);
        }

		@Override
		public Object createCache(String request) {
			// TODO Auto-generated method stub
			return null;
		}
    }
}
