package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.util.TemporaryFiles;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MemoryMappedCompare {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(MemoryMappedCompare.class);

    private static final int BUF_SIZE = 8192;

    private static final int FILE_SIZE = 1024 * 1024 * 1024 * 1;

    private static final double NANOS_PER_SEC = TimeUnit.SECONDS.toNanos(1);

    private static boolean areFilesIdenticalStreams(final Path a, final Path b) throws IOException {

        try (final FileInputStream fisa = new FileInputStream(a.toFile());
             final BufferedInputStream bisa = new BufferedInputStream(fisa, BUF_SIZE);
             final FileInputStream fisb = new FileInputStream(a.toFile());
             final BufferedInputStream bisb = new BufferedInputStream(fisb, BUF_SIZE)) {

            int ba, bb = Integer.MIN_VALUE;

            while ((ba = bisa.read()) >= 0 && (bb = bisb.read()) >= 0) {
                if ((byte)ba != (byte)bb) return false;
            }

            return ba == -1 && bb == -1;

        }

    }

    private static boolean areFilesIdenticalChannels(final Path a, final Path b) throws IOException {

        try (final FileChannel fca = FileChannel.open(a, StandardOpenOption.READ);
             final FileChannel fcb = FileChannel.open(b, StandardOpenOption.READ)) {

            if (fca.size() == 0 && fcb.size() == 0) return true;
            else if (fca.size() != fcb.size()) return false;

            final ByteBuffer bba = ByteBuffer.allocateDirect(BUF_SIZE);
            final ByteBuffer bbb = ByteBuffer.allocateDirect(BUF_SIZE);

            while (fca.position() < fca.size() || fcb.position() < fcb.size()) {

                bba.clear();
                bbb.clear();

                while (bba.hasRemaining() && fca.read(bba) >= 0);
                while (bbb.hasRemaining() && fcb.read(bbb) >= 0);

                if (!bba.equals(bbb)) return false;

            }

        }

        return true;

    }


    private static boolean areFilesIdenticalMemoryMapped(final Path a, final Path b) throws IOException {
        try (final FileChannel fca = FileChannel.open(a, StandardOpenOption.READ);
             final FileChannel fcb = FileChannel.open(b, StandardOpenOption.READ)) {
            final MappedByteBuffer mbba = fca.map(FileChannel.MapMode.READ_ONLY, 0, fca.size());
            final MappedByteBuffer mbbb = fcb.map(FileChannel.MapMode.READ_ONLY, 0, fcb.size());
            return mbba.equals(mbbb);
        }
    }

    public static void main(final String[] args) throws IOException {

        final Path matchedA = temporaryFiles.createTempFile("matched", "garbage");
        final Path matchedB = temporaryFiles.createTempFile("matched", "garbage");
        final Path unmatchedA = temporaryFiles.createTempFile("unmatched", "garbage");
        final Path unmatchedB = temporaryFiles.createTempFile("unmmatched", "garbage");

        fillWithGarbage(unmatchedA);
        fillWithGarbage(unmatchedB);
        fillWithIdenticalGarbage(matchedA, matchedB);

        testPaths(matchedA, matchedB, "IO streams", MemoryMappedCompare::areFilesIdenticalStreams);
        testPaths(matchedA, matchedB, "nio channels", MemoryMappedCompare::areFilesIdenticalChannels);
        testPaths(matchedA, matchedB, "memory mapping", MemoryMappedCompare::areFilesIdenticalMemoryMapped);

        testPaths(unmatchedA, unmatchedB, "IO streams", MemoryMappedCompare::areFilesIdenticalStreams);
        testPaths(unmatchedA, unmatchedB, "nio channels", MemoryMappedCompare::areFilesIdenticalChannels);
        testPaths(unmatchedA, unmatchedB, "memory mapping", MemoryMappedCompare::areFilesIdenticalMemoryMapped);

        Files.delete(matchedA);
        Files.delete(matchedB);
        Files.delete(unmatchedA);
        Files.delete(unmatchedB);

    }

    private static void testPaths(final Path a, final Path b, final String strategy, final IOOP op) throws IOException {
        final long begin = System.nanoTime();
        final boolean identical = op.execute(a, b);
        final double elapsed = ((double) (System.nanoTime() - begin)) / NANOS_PER_SEC;
        System.out.printf("Files %s and %s match: %b. Using %s it took %f seconds.%n",
                          a, b, identical, strategy, elapsed);
    }

    private static void fillWithGarbage(final Path path) throws IOException {

        final Random random = new Random();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[1024 * 1024]);

        try (final FileChannel fc = FileChannel.open(path, StandardOpenOption.WRITE)) {

            for (int i = 0; i < FILE_SIZE; i += byteBuffer.capacity()) {
                byteBuffer.clear();
                random.nextBytes(byteBuffer.array());
                while(byteBuffer.hasRemaining()) fc.write(byteBuffer);
            }

            if (fc.size() != FILE_SIZE) throw new RuntimeException("I've made a huge mistake.");

        }

    }

    private static void fillWithIdenticalGarbage(final Path a, final Path b) throws IOException {

        final Random random = new Random();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[1024 * 1024]);

        try (final FileChannel fca = FileChannel.open(a, StandardOpenOption.WRITE);
             final FileChannel fcb = FileChannel.open(b, StandardOpenOption.WRITE)) {

            for (int i = 0; i < FILE_SIZE; i += byteBuffer.capacity()) {

                byteBuffer.clear();
                random.nextBytes(byteBuffer.array());

                byteBuffer.rewind();
                while(byteBuffer.hasRemaining()) fca.write(byteBuffer);

                byteBuffer.rewind();
                while(byteBuffer.hasRemaining()) fcb.write(byteBuffer);

            }

            if (fca.size() != FILE_SIZE) throw new RuntimeException("I've made a huge mistake.");
            if (fcb.size() != FILE_SIZE) throw new RuntimeException("I've made a huge mistake.");

        }

    }

    @FunctionalInterface
    private interface IOOP {
        boolean execute(Path a, Path b) throws IOException;
    }

}
