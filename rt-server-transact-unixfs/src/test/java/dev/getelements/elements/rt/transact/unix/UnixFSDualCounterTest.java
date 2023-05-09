package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.transact.FatalException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.getelements.elements.rt.transact.unix.UnixFSDualCounter.pack;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.*;

public class UnixFSDualCounterTest {

    public static final int MAX_VALUE = 4096;

    private final UnixFSDualCounter counter;

    public UnixFSDualCounterTest(final UnixFSDualCounter counter) {
        this.counter = counter;
    }

    @Factory
    public static Object[] getTestInstances() throws Exception {
        return new Object[]{
            javaAPITest(),
            memoryMappedTest()
        };
    }

    private static UnixFSDualCounterTest memoryMappedTest() throws IOException {

        final Path temp = Files.createTempFile(UnixFSDualCounterTest.class.getSimpleName(), "bin");

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {

            fileChannel.write(ByteBuffer.allocate(Long.BYTES));

            final ByteBuffer mapped = fileChannel.map(READ_WRITE, 0, Long.BYTES);

            final UnixFSAtomicLong atomicLong = UnixFSMemoryUtils.getInstance().getAtomicLong(mapped);
            atomicLong.set(pack(MAX_VALUE, MAX_VALUE));

            final UnixFSDualCounter counter = new UnixFSDualCounter(MAX_VALUE, atomicLong);
            return new UnixFSDualCounterTest(counter);

        }

    }

    private static UnixFSDualCounterTest javaAPITest() {
        final UnixFSDualCounter counter = new UnixFSDualCounter(MAX_VALUE, UnixFSAtomicLong.basic()).reset();
        return new UnixFSDualCounterTest(counter);
    }

    @BeforeMethod
    public void reset() {
        counter.reset();
    }

    @Test
    public void testConsumeAll() {
        assertTrue(counter.isEmpty());
        for (int i = 0; i <= MAX_VALUE; ++i) assertEquals(counter.incrementLeadingAndGet(), i);
        assertTrue(counter.isFull());
    }

    @Test
    public void testConsumePartially() {
        assertTrue(counter.isEmpty());
        for (int i = 0; i <= MAX_VALUE / 2; ++i) assertEquals(counter.incrementLeadingAndGet(), i);
        assertTrue(!counter.isFull());
        assertTrue(!counter.isEmpty());
    }

    @Test
    public void testOverflow() {

        for (int i = 0; i <= MAX_VALUE; ++i) assertEquals(counter.incrementLeadingAndGet(), i);

        try {
            counter.incrementLeadingAndGet();
            fail("Expected exception.");
        } catch (FatalException ex) {
            assertEquals(counter.getLeading(), MAX_VALUE);
            assertEquals(counter.getTrailing(), 0);
        }

    }

    @Test
    public void testBalanced() {

        final int count = MAX_VALUE / 2;

        assertTrue(counter.isEmpty());
        for (int i = 0; i <= count; ++i) assertEquals(counter.incrementLeadingAndGet(), i);
        assertTrue(!counter.isFull());

        assertTrue(!counter.isEmpty());
        for (int i = 0; i <= count; ++i) assertEquals(counter.getTrailingAndIncrement(), i);
        assertTrue(counter.isEmpty());

        assertEquals(counter.getLeading(), count);
        assertEquals(counter.getTrailing(), count);

    }

    @Test
    public void testUnderflow() {

        final int count = MAX_VALUE / 2;
        for (int i = 0; i <= count; ++i) assertEquals(counter.incrementLeadingAndGet(), i);
        for (int i = 0; i <= count; ++i) assertEquals(counter.getTrailingAndIncrement(), i);

        try {
            counter.getTrailingAndIncrement();
            fail("Expected exception.");
        } catch (IllegalStateException ex) {
            assertEquals(counter.getLeading(), MAX_VALUE / 2);
            assertEquals(counter.getTrailing(), MAX_VALUE / 2);
        }

    }

    @Test
    public void testSizeCalculates() {

        final int count = MAX_VALUE / 2;
        for (int i = 0; i <= count; ++i) assertEquals(counter.incrementLeadingAndGet(), i);
        for (int i = 0; i <= count; ++i) assertEquals(counter.getTrailingAndIncrement(), i);

    }

}

