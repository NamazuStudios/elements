package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.util.ReentrantThreadLocal;
import org.testng.annotations.Test;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

public class ReentrantThreadLocalTest {

    private ReentrantThreadLocal<UUID> underTest = new ReentrantThreadLocal<>();

    @Test(invocationCount = 1, threadPoolSize = 1024)
    public void testEnterExit() {

        try {
            underTest.getCurrent();
        } catch (IllegalStateException ex) {
            underTest.ensureEmpty();
        }

        var uuid = randomUUID();

        try (var cxt = underTest.enter(uuid)) {
            assertEquals(uuid, cxt.get());
            doRecursive(2048, cxt.get(), randomUUID());
        }

        underTest.ensureEmpty();

        try {
            underTest.getCurrent();
        } catch (IllegalStateException ex) {
            underTest.ensureEmpty();
        }

    }

    private void doRecursive(final int remaining, final UUID upper, final UUID uuid) {

        if (remaining < 0) return;

        try (var cxt = underTest.enter(uuid)) {
            assertEquals(cxt.get(), uuid);
            doRecursive(remaining - 1, cxt.get(), randomUUID());
        }

        assertEquals(underTest.getCurrent(), upper);

    }

}