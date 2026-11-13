package com.company.cvhsm.hsm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseRouterTest {

    private ResponseRouter router;

    @BeforeEach
    void setUp() {
        router = new ResponseRouter();
    }

    @AfterEach
    void tearDown() {
        router.shutdown();
    }

    @Test
    void completesRegisteredFutureWhenResponseArrives() throws Exception {
        CompletableFuture<HsmResponse> future = router.register("ABCD", 5000);

        HsmResponse response = HsmResponse.builder()
                .header("ABCD")
                .responseCode("B3")
                .errorCode("00")
                .body(new byte[0])
                .build();

        router.complete(response);

        HsmResponse result = future.get(1, TimeUnit.SECONDS);
        assertEquals("ABCD", result.getHeader());
        assertEquals("B3", result.getResponseCode());
        assertEquals("00", result.getErrorCode());
        assertFalse(router.hasPending("ABCD"));
    }

    @Test
    void failsRegisteredFutureOnTimeout() {
        CompletableFuture<HsmResponse> future = router.register("TIME", 50);

        ExecutionException exception = assertThrows(
                ExecutionException.class, () -> future.get(1, TimeUnit.SECONDS));
        assertTrue(exception.getCause() instanceof TimeoutException);
        assertFalse(router.hasPending("TIME"));
    }

    @Test
    void cancelsTimeoutWhenResponseArrivesEarly() throws Exception {
        CompletableFuture<HsmResponse> future = router.register("FAST", 5000);

        HsmResponse response = HsmResponse.builder()
                .header("FAST")
                .responseCode("B3")
                .errorCode("00")
                .body(new byte[0])
                .build();

        router.complete(response);

        HsmResponse result = future.get(1, TimeUnit.SECONDS);
        assertEquals("FAST", result.getHeader());
        assertFalse(router.hasPending("FAST"));
        // Wait briefly to confirm no later timeout task causes issues.
        Thread.sleep(100);
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void ignoresUnknownOrLateResponse() {
        HsmResponse response = HsmResponse.builder()
                .header("MISS")
                .responseCode("B3")
                .errorCode("00")
                .body(new byte[0])
                .build();

        router.complete(response);
        assertFalse(router.hasPending("MISS"));
    }
}
