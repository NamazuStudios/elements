package com.namazustudios.socialengine.rt.transact.unix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;

public class UnixFSDualCounterSortingTest {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSDualCounterSortingTest.class);

    @DataProvider
    public Object[][] getTestData() {
        return new Object[][] {
//            normalDataSetFilled(),
            offsetDataSetFilled()
        };
    }

    private Object[] normalDataSetFilled() {
        final UnixFSDualCounter counter = new UnixFSDualCounter(500);
        final List<UnixFSDualCounter.Snapshot> snapshots = fill(counter);
        return new Object[]{snapshots.get(0), snapshots};
    }

    private Object[] offsetDataSetFilled() {
        final UnixFSDualCounter counter = new UnixFSDualCounter(500);
        for (int i = 0; i < 100; ++i) counter.incrementAndGetLeading();
        for (int i = 0; i < 100; ++i) counter.incrementAndGetTrailing();
        final List<UnixFSDualCounter.Snapshot> snapshots = fill(counter);
        return new Object[]{snapshots.get(0), snapshots};
    }


    private List<UnixFSDualCounter.Snapshot> fill(final UnixFSDualCounter counter) {

        final List<UnixFSDualCounter.Snapshot> snapshots = new ArrayList<>();

        while (!counter.isFull()) {
            counter.incrementAndGetLeading();
            snapshots.add(counter.getSnapshot());
        }

        return snapshots;

    }

    @Test(invocationCount = 1000, dataProvider = "getTestData")
    public void monteCarloTestSorting(final UnixFSDualCounter.Snapshot reference,
                                      final List<UnixFSDualCounter.Snapshot> snapshots) {

        assertTrue(!snapshots.isEmpty(), "Empty data set.");

        final Random random = ThreadLocalRandom.current();

        final int lIndex = random.nextInt(snapshots.size());
        final int rIndex = random.nextInt(snapshots.size());

        final UnixFSDualCounter.Snapshot lValue = snapshots.get(lIndex);
        final UnixFSDualCounter.Snapshot rValue = snapshots.get(rIndex);

        logger.info("Testing {} compareTo {}", lValue, rValue);

        if (lIndex == rIndex) {
            final String condition = format("expected =0 when comparing %s to %s", lValue, rValue);
            assertTrue(lValue.compareTo(reference, rValue) == 0, condition);
        } else if (lIndex < rIndex) {
            final String condition = format("expected <0 when comparing %s to %s", lValue, rValue);
            assertTrue(lValue.compareTo(reference, rValue) < 0, condition);
        } else if (lIndex > rIndex) {
            final String condition = format("expected >0 when comparing %s to %s", lValue, rValue);
            assertTrue(lValue.compareTo(reference, rValue) > 0, condition);
        }

    }

}
