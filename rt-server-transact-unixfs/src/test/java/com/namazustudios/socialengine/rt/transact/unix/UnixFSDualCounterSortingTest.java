package com.namazustudios.socialengine.rt.transact.unix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSDualCounter.pack;
import static java.lang.String.format;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertTrue;

public class UnixFSDualCounterSortingTest {

    private static final int MAX_VALUE = 500;

    private static final Logger logger = LoggerFactory.getLogger(UnixFSDualCounterSortingTest.class);

    private final CounterSupplier counterSupplier;

    public UnixFSDualCounterSortingTest(final CounterSupplier counterSupplier) {
        this.counterSupplier = counterSupplier;
    }

    @Factory
    public static Object[] getInstances() {
        return new Object[] {
            javaAPITest(),
            memoryMappedTest()
        };
    }

    private static UnixFSDualCounterSortingTest javaAPITest() {
        return new UnixFSDualCounterSortingTest(max -> new UnixFSDualCounter(max).reset());
    }

    private static UnixFSDualCounterSortingTest memoryMappedTest() {
        return new UnixFSDualCounterSortingTest(maxValue -> {

            final Path temp = Files.createTempFile(UnixFSDualCounterStreamTest.class.getSimpleName(), "bin");

            try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {

                fileChannel.write(ByteBuffer.allocate(Long.BYTES));

                final ByteBuffer mapped = fileChannel.map(READ_WRITE, 0, Long.BYTES);

                final UnixFSAtomicLong atomicLong = UnixFSMemoryUtils.getInstance().getAtomicLong(mapped);
                atomicLong.set(pack(maxValue, maxValue));

                return new UnixFSDualCounter(maxValue, atomicLong).reset();
            }

        });
    }

    @DataProvider
    public Object[][] getTestData() throws Exception {
        return new Object[][] {
            normalDataSetFilled(),
            offsetDataSetFilled()
        };
    }

    private Object[] normalDataSetFilled() throws Exception {
        final UnixFSDualCounter counter = counterSupplier.supply(MAX_VALUE);
        final List<UnixFSDualCounter.Snapshot> snapshots = fill(counter);
        return new Object[]{counter.getTrailing(), snapshots};
    }

    private Object[] offsetDataSetFilled() throws Exception {
        final UnixFSDualCounter counter = counterSupplier.supply(MAX_VALUE);
        for (int i = 0; i < 100; ++i) counter.incrementLeadingAndGet();
        for (int i = 0; i < 100; ++i) counter.getTrailingAndIncrement();
        final List<UnixFSDualCounter.Snapshot> snapshots = fill(counter);
        return new Object[]{counter.getTrailing(), snapshots};
    }

    private List<UnixFSDualCounter.Snapshot> fill(final UnixFSDualCounter counter) {

        final List<UnixFSDualCounter.Snapshot> snapshots = new ArrayList<>();

        while (!counter.isFull()) {
            counter.incrementLeadingAndGet();
            snapshots.add(counter.getSnapshot());
        }

        return snapshots;

    }

    @Test(invocationCount = 1000, dataProvider = "getTestData")
    public void monteCarloTestSorting(final int reference,
                                      final List<UnixFSDualCounter.Snapshot> snapshots) {

        assertTrue(!snapshots.isEmpty(), "Empty data set.");

        final Random random = ThreadLocalRandom.current();

        final int lIndex = random.nextInt(snapshots.size());
        final int rIndex = random.nextInt(snapshots.size());

        final UnixFSDualCounter.Snapshot lValue = snapshots.get(lIndex);
        final UnixFSDualCounter.Snapshot rValue = snapshots.get(rIndex);

        logger.debug("Testing {} compareTo {}", lValue, rValue);

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

    @FunctionalInterface
    private interface CounterSupplier {
        UnixFSDualCounter supply(int maxValue) throws Exception;
    }

}
