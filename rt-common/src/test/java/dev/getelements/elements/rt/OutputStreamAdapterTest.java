package dev.getelements.elements.rt;

import dev.getelements.elements.rt.util.OutputStreamAdapter;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertEquals;

public class OutputStreamAdapterTest {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(OutputStreamAdapterTest.class);

    private static final int BLOCKS_TO_WRITE = 1024;

    private static final int WRITE_OPERATIONS = 1024 * 8;

    private final BiFunction<WritableByteChannel, ByteBuffer, OutputStreamAdapter> supplier;

    @Factory
    public static Object[] getTestObjects() {
        return IntStream
            .range(0, 3)
            .mapToObj(flags -> new OutputStreamAdapterTest((wbc, bb) -> new OutputStreamAdapter(wbc, bb, flags)))
            .toArray();
    }

    public OutputStreamAdapterTest(final BiFunction<WritableByteChannel, ByteBuffer, OutputStreamAdapter> supplier) {
        this.supplier = supplier;
    }

    @AfterClass
    public static void cleanup() {
        temporaryFiles.deleteTempFilesAndDirectories();
    }

    @Test
    public void testWriteSingleByte() throws IOException {

        final java.nio.file.Path testFilePath = temporaryFiles.createTempFile();
        final java.nio.file.Path referenceFilePath = temporaryFiles.createTempFile();

        final Random random = ThreadLocalRandom.current();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[4096]);

        try (final FileChannel test = FileChannel.open(testFilePath, WRITE);
             final FileChannel reference = FileChannel.open(referenceFilePath, WRITE);
             final OutputStreamAdapter os = supplier.apply(test, allocateDirect(4096))) {

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

        final java.nio.file.Path testFilePath = temporaryFiles.createTempFile();
        final java.nio.file.Path referenceFilePath = temporaryFiles.createTempFile();

        final Random random = ThreadLocalRandom.current();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[4096]);

        try (final FileChannel test = FileChannel.open(testFilePath, WRITE);
             final FileChannel reference = FileChannel.open(referenceFilePath, WRITE);
             final OutputStreamAdapter os = supplier.apply(test, allocateDirect(4096))) {

            for (int i = 0; i < BLOCKS_TO_WRITE; ++i) {

                byteBuffer.clear();
                random.nextBytes(byteBuffer.array());

                os.write(byteBuffer.array());
                while (byteBuffer.hasRemaining()) reference.write(byteBuffer);

            }

        }

        assertFilesEqual(testFilePath, referenceFilePath);

    }

    @Test(invocationCount = 10, threadPoolSize = 10)
    public void testMixedWrites() throws IOException {

        final java.nio.file.Path testFilePath = temporaryFiles.createTempFile();
        final java.nio.file.Path referenceFilePath = temporaryFiles.createTempFile();

        final Random random = ThreadLocalRandom.current();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[1024 * 8]);

        try (final FileChannel test = FileChannel.open(testFilePath, WRITE);
             final FileChannel reference = FileChannel.open(referenceFilePath, WRITE);
             final OutputStreamAdapter os = supplier.apply(test, allocateDirect(4096))) {

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

}
