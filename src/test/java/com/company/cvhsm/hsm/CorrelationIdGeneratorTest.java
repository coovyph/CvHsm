package com.company.cvhsm.hsm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorrelationIdGeneratorTest {

    private CorrelationIdGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CorrelationIdGenerator();
    }

    @Test
    void generatesFourCharacterUppercaseHexHeader() {
        String header = generator.generate();

        assertEquals(4, header.length());
        assertTrue(header.matches("[0-9A-F]{4}"), "Header should be uppercase hex: " + header);
    }

    @Test
    void generatesSequentialHeaders() {
        assertEquals("0001", generator.generate());
        assertEquals("0002", generator.generate());
        assertEquals("0003", generator.generate());
    }

    @Test
    void wrapsFromMaxToOne() throws Exception {
        setSequenceValue(0xFFFE);

        assertEquals("FFFF", generator.generate());
        assertEquals("0001", generator.generate());
    }

    @Test
    void concurrentGenerationProducesNoDuplicates() throws InterruptedException, ExecutionException {
        int count = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                futures.add(executor.submit(() -> generator.generate()));
            }

            Set<String> headers = new HashSet<>();
            for (Future<String> future : futures) {
                headers.add(future.get());
            }

            assertEquals(count, headers.size(), "Concurrent generation produced duplicate headers");
        } finally {
            executor.shutdown();
        }
    }

    private void setSequenceValue(int value) throws Exception {
        Field sequenceField = CorrelationIdGenerator.class.getDeclaredField("sequence");
        sequenceField.setAccessible(true);
        AtomicInteger sequence = (AtomicInteger) sequenceField.get(generator);
        sequence.set(value);
    }
}
