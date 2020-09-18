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
//            javaAPITest(),
            memoryMappedTest()
        };
    }

    private static UnixFSDualCounterStreamTest javaAPITest() {
        return new UnixFSDualCounterStreamTest(new UnixFSDualCounter(MAX_VALUE).reset());
    }

    private static UnixFSDualCounterStreamTest memoryMappedTest() throws IOException {

        final Path temp = Files.createTempFile(UnixFSDualCounterStreamTest.class.getSimpleName(), "bin");

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {

            fileChannel.write(ByteBuffer.allocate(Long.BYTES));

            final ByteBuffer mapped = fileChannel.map(READ_WRITE, 0, Long.BYTES);

            final UnixFSAtomicLong atomicLong = UnixFSMemoryUtils.getInstance().getAtomicLong(mapped);
            atomicLong.set(pack(MAX_VALUE, MAX_VALUE));

            final UnixFSDualCounter counter = new UnixFSDualCounter(MAX_VALUE, atomicLong).reset();
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
        assertEquals(counter.getLeading(), 0);
        assertEquals(counter.getTrailing(), 0);
    }

    @Test(dependsOnMethods = "testInitial")
    public void testIncrement() {
        for (int i = 0; i < 99; ++i) assertEquals(counter.incrementLeadingAndGet(), i);
    }

    @Test(dependsOnMethods = "testIncrement")
    public void testRange() {

        final int expected[] = new int[99];
        for (int i = 0; i < 99; ++i) expected[i] = i;

        final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
        final int[] actual = snapshot.range().toArray();

        assertEquals(actual, expected);

    }

    @Test(dependsOnMethods = "testIncrement")
    public void testRangeReverse() {

        final int expected[] = new int[99];
        for (int i = 0; i < 99; ++i) expected[i] = 98 - i;

        final UnixFSDualCounter.Snapshot snapshot = counter.getSnapshot();
        final int[] actual = snapshot.reverseRange().toArray();

        assertEquals(actual, expected);

    }

}
