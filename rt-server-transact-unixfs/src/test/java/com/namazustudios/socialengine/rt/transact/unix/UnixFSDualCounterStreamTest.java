package com.namazustudios.socialengine.rt.transact.unix;

import org.testng.annotations.Test;

import static java.lang.Integer.MAX_VALUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class UnixFSDualCounterStreamTest {

    private UnixFSDualCounter counter = new UnixFSDualCounter(MAX_VALUE);

    @Test
    public void testInitial() {
        assertFalse(counter.getSnapshot().range().findFirst().isPresent(), "Expected empty range.");
        assertFalse(counter.getSnapshot().reverseRange().findFirst().isPresent(), "Expected empty range.");
        assertEquals(counter.getLeading(), MAX_VALUE);
        assertEquals(counter.getTrailing(), MAX_VALUE);
    }

    @Test(dependsOnMethods = "testInitial")
    public void testIncrement() {
        final int expected[] = new int[99];
        for (int i = 0; i < 99; ++i) expected[i] = i;
        for (int i = 0; i < 99; ++i) assertEquals(counter.incrementAndGetLeading(), expected[i]);
    }

    @Test(dependsOnMethods = "testIncrement")
    public void testRange() {

        final int expected[] = new int[100];
        for (int i = 0; i < 100; ++i) expected[i] = i == 0 ? MAX_VALUE : i - 1;

        final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
        assertEquals(expected, snapshot.range().toArray());

    }

    @Test(dependsOnMethods = "testIncrement")
    public void testRangeReverse() {

        final int expected[] = new int[100];
        for (int i = 0; i < 100; ++i) expected[i] = i == 99 ? MAX_VALUE : 98 - i;

        final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
        assertEquals(expected, snapshot.reverseRange().toArray());

    }

}
