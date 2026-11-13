package com.company.cvhsm.hsm;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates sequential 4-character uppercase hex correlation headers for HSM requests.
 *
 * <p>The sequence ranges from 0x0001 to 0xFFFF and wraps back to 0x0001 after 0xFFFF.
 * The counter is thread-safe via {@link AtomicInteger}.
 */
@Component
public class CorrelationIdGenerator {

    private static final int MAX_SEQUENCE = 0xFFFF;

    private final AtomicInteger sequence = new AtomicInteger(0);

    public String generate() {
        int next = sequence.updateAndGet(current -> current >= MAX_SEQUENCE ? 1 : current + 1);
        return String.format(Locale.US, "%04X", next);
    }
}
