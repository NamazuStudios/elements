package com.namazustudios.socialengine.rt.transact.unix;


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
import static java.nio.file.Files.createTempFile;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertEquals;

public class UnixFSMemoryUtilsTest {

    private static final int TEST_COUNT = 16;

    private final UnixFSMemoryUtils underTest = UnixFSMemoryUtils.getInstance();

    private Path temp;

    private MappedByteBuffer buffer;

    private List<UnixFSAtomicCASCounter> counters;

    @DataProvider
    public Object[][] getCounters() throws Exception {

        temp = createTempFile(getClass().getSimpleName(), "bin");

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {
            final ByteBuffer filler = ByteBuffer.allocateDirect(Long.BYTES * TEST_COUNT);
            while (filler.hasRemaining()) filler.putLong(0);
            filler.rewind();
            fileChannel.write(filler);
            buffer = fileChannel.map(READ_WRITE, 0, Long.BYTES * TEST_COUNT);
        }

        counters = new ArrayList<>();

        for (int i = 0; i < TEST_COUNT; ++i) {
            final UnixFSAtomicCASCounter counter = underTest.getCounter(mappedByteBuffer);
            counters.add(counter);
        }

        return counters.stream()
            .map(counter -> new Object[]{counter})
            .toArray(Object[][]::new);

    }

    @Test(threadPoolSize = TEST_COUNT * 2, dataProvider = "getCounters", invocationCount = 1000)
    public void testCounter(final UnixFSAtomicCASCounter counter) {

        final ThreadLocalRandom random = ThreadLocalRandom.current();

        long expect;
        long update = 0;

        do {
            expect = counter.get();
            assertEquals(update, expect);
            update = random.nextLong();
        } while (!counter.compareAndSet(expect, update));

    }

    @Test
    public void testFileWasWritten() {

    }

}
