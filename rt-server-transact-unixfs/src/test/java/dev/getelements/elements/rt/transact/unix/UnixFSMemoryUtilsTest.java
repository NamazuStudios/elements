package dev.getelements.elements.rt.transact.unix;


import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertEquals;

public class UnixFSMemoryUtilsTest {

    private static final int TEST_COUNT = 16;

    private static final Logger logger = LoggerFactory.getLogger(UnixFSMemoryUtilsTest.class);

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(UnixFSMemoryUtilsTest.class);

    private final UnixFSMemoryUtils underTest = UnixFSMemoryUtils.getInstance();

    private Path temp;

    private MappedByteBuffer memoryMappedBuffer;

    private List<UnixFSAtomicLong> counters;

    @BeforeClass
    public void setup() throws Exception {

        temp = temporaryFiles.createTempFile(getClass().getSimpleName(), "bin");

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {
            final ByteBuffer filler = ByteBuffer.allocateDirect(Long.BYTES * TEST_COUNT);
            while (filler.hasRemaining()) filler.putLong(0);
            filler.rewind();
            fileChannel.write(filler);
            memoryMappedBuffer = fileChannel.map(READ_WRITE, 0, Long.BYTES * TEST_COUNT);
        }

        memoryMappedBuffer.rewind();
        counters = new ArrayList<>();

        for (int i = 0; i < TEST_COUNT; ++i) {
            final UnixFSAtomicLong counter = underTest.getAtomicLong(memoryMappedBuffer);
            counters.add(counter);
        }

    }

    @DataProvider
    public Object[][] getCounters() throws Exception {

        return counters.stream()
            .map(counter -> new Object[]{counter})
            .toArray(Object[][]::new);

    }

    @Test(threadPoolSize = TEST_COUNT * 2, dataProvider = "getCounters", invocationCount = 1000)
    public void testCounter(final UnixFSAtomicLong counter) {

        final ThreadLocalRandom random = ThreadLocalRandom.current();

        long expect;
        long update;

        do {
            expect = counter.get();
            update = random.nextLong();
            if (random.nextInt(25) == 0) System.gc();
        } while (!counter.compareAndSet(expect, update));

    }

    @Test(dependsOnMethods = "testCounter")
    public void testBufferIsConsistent() {
        memoryMappedBuffer.rewind();
        checkBuffer(memoryMappedBuffer);
    }

    @Test(dependsOnMethods = "testBufferIsConsistent")
    public void testFileIsConsistent() throws Exception {

        memoryMappedBuffer.force();

        final ByteBuffer byteBuffer = ByteBuffer.allocate(TEST_COUNT * Long.BYTES);

        try (final FileChannel fileChannel = FileChannel.open(temp, READ)) {
            fileChannel.read(byteBuffer);
        }

        byteBuffer.rewind();
        checkBuffer(byteBuffer);

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMisalignedFails() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(Long.BYTES + 1);
        underTest.getAtomicLong(buffer, 1);
    }

    private void checkBuffer(final ByteBuffer buffer) {
        for (final UnixFSAtomicLong counter : counters) {
            final long cValue = counter.get();
            final long bValue = buffer.getLong();
            logger.debug("Buffer value {}. Counter value {}.", bValue, cValue);
            assertEquals(bValue, cValue, "Buffer value mismatch.");
        }
    }

}
