package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.util.OutputStreamAdapter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.String.format;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class OutputStreamAdapterTest {

    private static final int BLOCKS_TO_WRITE = 1024;

    private static final int WRITE_OPERATIONS = 1024 * 8;

    private final Deque<java.nio.file.Path> paths = new ConcurrentLinkedDeque<>();

    @AfterClass
    public void cleanup() {
        paths.forEach(p -> {
            try {
                Files.delete(p);
            } catch (IOException ex) {
                fail("Failed to clean up.", ex);
            }
        });
    }


    @Test
    public void testWriteSingleByte() throws IOException {

        final java.nio.file.Path testFilePath = createTemporary();
        final java.nio.file.Path referenceFilePath = createTemporary();

        final Random random = ThreadLocalRandom.current();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[4096]);

        try (final FileChannel test = FileChannel.open(testFilePath, WRITE);
             final FileChannel reference = FileChannel.open(referenceFilePath, WRITE);
             final OutputStreamAdapter os = new OutputStreamAdapter(test, allocateDirect(4096), 0)) {

            for (int i = 0; i < BLOCKS_TO_WRITE; ++i) {
                byteBuffer.clear();
                random.nextBytes(byteBuffer.array());

                byteBuffer.rewind();
                while (byteBuffer.hasRemaining()) os.write(byteBuffer.get());

                byteBuffer.rewind();
                while (byteBuffer.hasRemaining()) reference.write(byteBuffer);
            }

        }

        assertFilesEqual(testFilePath, referenceFilePath);

    }

    @Test
    public void testWriteBufferArray() throws IOException {

        final java.nio.file.Path testFilePath = createTemporary();
        final java.nio.file.Path referenceFilePath = createTemporary();

        final Random random = ThreadLocalRandom.current();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[4096]);

        try (final FileChannel test = FileChannel.open(testFilePath, WRITE);
             final FileChannel reference = FileChannel.open(referenceFilePath, WRITE);
             final OutputStreamAdapter os = new OutputStreamAdapter(test, allocateDirect(4096), 0)) {

            for (int i = 0; i < BLOCKS_TO_WRITE; ++i) {

                byteBuffer.clear();
                random.nextBytes(byteBuffer.array());

                os.write(byteBuffer.array());
                while (byteBuffer.hasRemaining()) reference.write(byteBuffer);

            }

        }

        assertFilesEqual(testFilePath, referenceFilePath);

    }

    @Test(invocationCount = 100, threadPoolSize = 16)
    public void testMixedWrites() throws IOException {

        final java.nio.file.Path testFilePath = createTemporary();
        final java.nio.file.Path referenceFilePath = createTemporary();

        final Random random = ThreadLocalRandom.current();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[1024 * 8]);

        try (final FileChannel test = FileChannel.open(testFilePath, WRITE);
             final FileChannel reference = FileChannel.open(referenceFilePath, WRITE);
             final OutputStreamAdapter os = new OutputStreamAdapter(test, allocateDirect(4096), 0)) {

            for (int i = 0; i < WRITE_OPERATIONS; ++i) {

                byteBuffer.clear();

                if (random.nextBoolean()) {

                    final byte datum = (byte) (random.nextInt() % Byte.MAX_VALUE);

                    byteBuffer.position(0).limit(1);
                    byteBuffer.put(datum);
                    byteBuffer.rewind();

                    os.write(datum);

                } else {

                    final int limit = random.nextInt(byteBuffer.capacity());

                    random.nextBytes(byteBuffer.array());
                    byteBuffer.limit(limit);

                    os.write(byteBuffer.array(), 0, limit);
                    reference.write(byteBuffer);

                }

                while (byteBuffer.hasRemaining()) reference.write(byteBuffer);

            }

        }

        assertFilesEqual(testFilePath, referenceFilePath);

    }


    private static void assertFilesEqual(final java.nio.file.Path a, final java.nio.file.Path b) throws IOException {

        try (final FileChannel fca = FileChannel.open(a, READ);
             final FileChannel fcb = FileChannel.open(b, READ)) {

            assertEquals(fca.size(), fcb.size(), format("File sizes must match %s != %s", a, b));

            final MappedByteBuffer mba = fca.map(READ_ONLY, 0, fca.size());
            final MappedByteBuffer mbb = fca.map(READ_ONLY, 0, fcb.size());
            assertEquals(mba, mbb, format("File contents must match %s != %s", a, b));

        }
    }

    private java.nio.file.Path createTemporary() throws IOException {
        final java.nio.file.Path path = createTempFile(OutputStreamAdapterTest.class.getSimpleName(), "garbage");
        paths.add(path);
        return path;
    }

}
