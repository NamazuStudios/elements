package dev.getelements.elements.rt.transact.unix;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Set;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.newSetFromMap;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class UnixFSCircularBlockBufferTest {

    private static final int BLOCK_SIZE = 1024;

    private static final int BLOCK_COUNT = 1024;

    private final UnixFSCircularBlockBuffer buffer;

    public UnixFSCircularBlockBufferTest(final UnixFSCircularBlockBuffer buffer) {
        this.buffer = buffer;
    }

    @Factory
    public static Object[] getInstances() throws IOException {
        return new Object[] {
            ramBased(),
            memoryMapped()
        };
    }

    private static UnixFSCircularBlockBufferTest ramBased() throws IOException {

        final UnixFSAtomicLong atomicLong = UnixFSAtomicLong.basic();
        final ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE * BLOCK_COUNT);

        final UnixFSCircularBlockBuffer circularBlockBuffer = new UnixFSCircularBlockBuffer(
            atomicLong,
            buffer,
            BLOCK_SIZE).reset();

        return new UnixFSCircularBlockBufferTest(circularBlockBuffer);

    }

    private static UnixFSCircularBlockBufferTest memoryMapped() throws IOException {

        final ByteBuffer header = ByteBuffer.allocate(Long.BYTES);
        final ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);

        while(header.hasRemaining()) header.put((byte)0xFF);
        while(buffer.hasRemaining()) buffer.put((byte)0xFF);

        final Path temp = Files.createTempFile(UnixFSDualCounterStreamTest.class.getSimpleName(), "bin");

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {

            header.rewind();
            fileChannel.write(header);

            for (int i = 0; i < BLOCK_COUNT; ++i) {
                buffer.rewind();
                fileChannel.write(buffer);
            }

            final ByteBuffer mapped = fileChannel.map(READ_WRITE, 0, fileChannel.size());
            mapped.position(0).limit(Long.BYTES);

            final UnixFSAtomicLong atomicLong = UnixFSMemoryUtils.getInstance().getAtomicLong(mapped.slice());
            mapped.position(Long.BYTES).limit((int)fileChannel.size());

            final UnixFSCircularBlockBuffer circularBlockBuffer = new UnixFSCircularBlockBuffer(
                    atomicLong,
                    mapped.slice(),
                    BLOCK_SIZE).reset();

            return new UnixFSCircularBlockBufferTest(circularBlockBuffer);

        }

    }

    @Test
    public void testBufferSlices() {

        final Set<ByteBuffer> slices = newSetFromMap(new IdentityHashMap<>());
        final UnixFSCircularBlockBuffer.View<ByteBuffer> rawView = buffer.rawView();

        assertTrue(rawView.isEmpty(), "Buffer should be initialized while empty.");

        for (int i = 0; i < BLOCK_COUNT; ++i) {
            if (!slices.add(rawView.nextLeading().getValue())) {
                fail("Got same buffer as was previously returned.");
            }
        }

        assertTrue(rawView.isFull(), "Buffer should be full.");

    }

}
