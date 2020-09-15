package com.namazustudios.socialengine.rt.transact.unix;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSDualCounter.pack;
import static java.lang.Integer.MAX_VALUE;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class UnixFSDualCounterStreamTest {

    private final UnixFSDualCounter counter;

    @Factory
    public static Object[] getTestInstances() throws Exception {
        return new Object[]{
            javaAPITest(),
            memoryMappedTest()
        };
    }

    private static UnixFSDualCounterStreamTest javaAPITest() {
        return new UnixFSDualCounterStreamTest(new UnixFSDualCounter(MAX_VALUE));
    }

    private static UnixFSDualCounterStreamTest memoryMappedTest() throws IOException {

        final Path temp = Files.createTempFile(UnixFSDualCounterStreamTest.class.getSimpleName(), "bin");

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {

            fileChannel.write(ByteBuffer.allocate(Long.BYTES));

            final ByteBuffer mapped = fileChannel.map(READ_WRITE, 0, Long.BYTES);

            final UnixFSAtomicLong atomicLong = UnixFSMemoryUtils.getInstance().getAtomicLong(mapped);
            atomicLong.set(pack(MAX_VALUE, MAX_VALUE));

            final UnixFSDualCounter counter = new UnixFSDualCounter(MAX_VALUE, atomicLong);
            return new UnixFSDualCounterStreamTest(counter);

        }

    }

    public UnixFSDualCounterStreamTest(final UnixFSDualCounter counter) {
        this.counter = counter;
    }

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
