package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertEquals;

public class UnixAtomicLongStructTest {

    @Test
    public void testAtomicLongInStruct() throws IOException {

        final TestStruct struct = new TestStruct();
        final Path temp = Files.createTempFile(getClass().getSimpleName(), ".bin");

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {
            final ByteBuffer bb = ByteBuffer.allocate(struct.size());
            while(bb.hasRemaining()) bb.put((byte)0xff);
            bb.rewind();
            fileChannel.write(bb);
            struct.setByteBuffer(fileChannel.map(READ_WRITE, 0, struct.size()), 0);
        }

        final UnixFSAtomicLong atomicLong = struct.atomicLongData.createAtomicLong();

        for (long l = 0; l < 100000; l++) {

            long value;

            do {
                value = atomicLong.get();
                assertEquals(value, l - 1);
            } while (!atomicLong.compareAndSet(value, value + 1));

        }

    }

    private class TestStruct extends Struct {

        final Signed64 unused = new Signed64(); // Intentional. We want to make sure that the struct properly aligns

        final UnixFSAtomicLongData atomicLongData = inner(new UnixFSAtomicLongData());

    }

}
