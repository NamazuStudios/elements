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
import static org.testng.Assert.assertTrue;

public class UnixAtomicLongStructTest {

    @Test
    public void testAtomicLongInStruct() throws IOException {

        final TestStruct struct = new TestStruct();
        final Path temp = Files.createTempFile(getClass().getSimpleName(), ".bin");

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {
            final ByteBuffer bb = ByteBuffer.allocate(struct.size());
            fileChannel.write(bb);
            struct.setByteBuffer(fileChannel.map(READ_WRITE, 0, struct.size()), 0);
        }

        final UnixFSAtomicLong atomicLong = struct.atomicLongData.createAtomicLong();

        for (long l = 0; l < 10000; l++) {

            long value;

            do {
                value = atomicLong.get();
                assertEquals(value, l);
            } while (!atomicLong.compareAndSet(value, value + 1));

        }

    }

    private class TestStruct extends Struct {

        final Signed64 foo = new Signed64();

        final UnixFSAtomicLongData atomicLongData = inner(new UnixFSAtomicLongData());

    }

}
