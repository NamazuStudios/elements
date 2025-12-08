package dev.getelements.elements.sdk.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class TestCodeGenerator {

    private final Set<String> codes = ConcurrentHashMap.newKeySet();

    private final AtomicInteger counter = new AtomicInteger(0);

    private final UniqueCodeGenerator generator = new UniqueCodeGenerator.Builder()
            .rejecting(codes::contains)
            .build();

    @DataProvider(parallel = true)
    public Object[][] length() {
        return new Object[][]{ {4}, {6}, {8}, {10}, {12} };
    }

    @Test(dataProvider = "length", invocationCount = 10000)
    public void testGeneration(final int length) {

        final var generated = generator
                .tryGenerateUniqueCode(length, 1024, codes::add)
                .orElseThrow(IllegalStateException::new);

        assertTrue(codes.contains(generated), "Expected code to be generated.");
        counter.incrementAndGet();

    }
    @Test(dataProvider = "length", invocationCount = 10000)
    public void testGenerationRejected(final int length) {

        final var generated = generator
                .tryGenerateUniqueCode(100, 50, s -> false)
                .isPresent();

        assertFalse(generated, "Expected code not to be generated.");

    }

    @Test(dataProvider = "length", invocationCount = 10000)
    public void testComputation(final int length) {

        final var generated = generator
                .tryComputeWithUniqueCode(length, 1024, s -> codes.add(s) ? Optional.of(s) : Optional.empty())
                .orElseThrow(IllegalStateException::new);

        assertTrue(codes.contains(generated), "Expected code to be generated.");
        counter.incrementAndGet();

    }

    @Test(dataProvider = "length", invocationCount = 10000)
    public void testComputationFails(final int length) {

        final var generated = generator
                .tryComputeWithUniqueCode(length, 1024, s -> Optional.empty())
                .isPresent();

        assertFalse(generated, "Expected code not to be generated.");

    }

    @Test(dependsOnMethods = {"testGeneration", "testGenerationRejected"})
    public void testAllGeneratedUnique() {
        assertEquals(codes.size(), counter.get(), "Expected all codes to be generated.");
    }

}
