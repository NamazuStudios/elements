package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.util.InputStreamAdapter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.min;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Arrays.fill;
import static org.testng.Assert.assertEquals;

public class InputStreamAdapterTest {

    private java.nio.file.Path testFile;

    @BeforeClass
    public void setupGarbageFile() throws IOException  {

        testFile = Files.createTempFile(InputStreamAdapterTest.class.getSimpleName(), "garbage");

        final Random random = ThreadLocalRandom.current();
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[1000000]);

        // We fill a file full of garbage bytes, so we can compare the reads from the adapter versus the core Java
        // APIs
        try (final FileChannel fc = FileChannel.open(testFile, WRITE)) {
            for (int i = 0; i < 500; ++i) {
                buffer.clear();
                random.nextBytes(buffer.array());
                fc.write(buffer);
            }
        }

    }

    @AfterClass
    public void destroyGarbageFile() throws IOException {
        Files.delete(testFile);
    }

    @Test
    public void testReadSingleByte() throws IOException {

        final ByteBuffer streamBuffer = allocateDirect(4096);

        try (final FileChannel channel = FileChannel.open(testFile, READ);
             final InputStreamAdapter is = new InputStreamAdapter(channel, streamBuffer, 0)) {
            final MappedByteBuffer reference = channel.map(READ_ONLY,0, channel.size());
            while (reference.hasRemaining()) assertEquals(reference.get(), (byte)is.read());
        }

    }

    @Test
    public void testReadBuffer() throws IOException {

        final byte[] isBuf = new byte[3000];
        final byte[] refBuf = new byte[isBuf.length];

        final ByteBuffer streamBuffer = allocateDirect(4096);

        try (final FileChannel channel = FileChannel.open(testFile, READ);
             final InputStreamAdapter is = new InputStreamAdapter(channel, streamBuffer, 0)) {
            final MappedByteBuffer reference = channel.map(READ_ONLY,0, channel.size());
            while (reference.hasRemaining()) {
                readFully(is, isBuf);
                reference.get(refBuf, 0, min(reference.remaining(), refBuf.length));
                assertEquals(refBuf, isBuf);
            }
        }

    }

    @Test(invocationCount = 100, threadPoolSize = 16)
    public void testMixedReads() throws IOException {

        // 25 kib buffers for testing
        final byte[] isBuf = new byte[1024 * 25];
        final byte[] refBuf = new byte[isBuf.length];

        final Random random = ThreadLocalRandom.current();
        final ByteBuffer streamBuffer = allocateDirect(4096);

        try (final FileChannel channel = FileChannel.open(testFile, READ);
             final InputStreamAdapter is = new InputStreamAdapter(channel, streamBuffer, 0)) {

            final MappedByteBuffer reference = channel.map(READ_ONLY,0, channel.size());

            while (reference.hasRemaining()) {
                // We are trying to inter-mix single-byte reads as would happen "in the wild".
                if (random.nextBoolean()) {
                    assertEquals(reference.get(), (byte) is.read());
                } else {

                    // We also ensure plenty of jagged reads passed through read loops which is likely how
                    // any client code would actaully read the.
                    final int length = min(reference.remaining(), random.nextInt(isBuf.length));
                    readFully(is, isBuf, 0, length);
                    reference.get(refBuf, 0, length);

                    // Dispose of any bytes that weren't read by simply inserting zeros
                    fill(isBuf, length, isBuf.length, (byte)0);
                    fill(refBuf, length, isBuf.length, (byte)0);
                    assertEquals(refBuf, isBuf);

                }
            }

            while (reference.hasRemaining()) {
                readFully(is, isBuf);
                reference.get(refBuf, 0, min(reference.remaining(), refBuf.length));
                assertEquals(refBuf, isBuf);
            }
        }

    }

    private static void readFully(final InputStream is,
                                  final byte[] b) throws IOException {
        readFully(is, b,0, b.length);
    }

    private static void readFully(final InputStream is,
                                  final byte[] b,
                                  final int offset,
                                  final int length) throws IOException {

        int off = offset, read = 0;

        while (read >= 0 && off < length)  {
            read = is.read(b, off, length - off);
            off += read;
        }

        if (read >= 0) assertEquals(off, length);

    }

}
