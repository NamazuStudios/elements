package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.Paths;
import dev.getelements.elements.rt.WeakReferenceLockSetService;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.getelements.elements.sdk.cluster.path.Paths.randomPath;
import static dev.getelements.elements.sdk.cluster.id.ResourceId.randomResourceId;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.toCollection;

public class WeakReferenceLockSetServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(WeakReferenceLockSetServiceTest.class);

    private final WeakReferenceLockSetService weakReferenceLockSetService = new WeakReferenceLockSetService();

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(WeakReferenceLockSetServiceTest.class);

    private static int MIN_PATH_COUNT = 100;

    private static int MAX_PATH_COUNT = 250;

    private static int MIN_RESOURCE_ID_COUNT = 50;

    private static int MAX_RESOURCE_ID_COUNT = 100;

    private static List<Path> testPaths = IntStream
            .range(0, 1000)
            .mapToObj(i -> randomPath())
            .collect(Collectors.toUnmodifiableList());

    private static List<ResourceId> testResourceIds = IntStream
            .range(0, 1000)
            .mapToObj(i -> randomResourceId())
            .collect(Collectors.toUnmodifiableList());

    @Test(threadPoolSize = 100, invocationCount = 10000)
    public void stressTestRandom() {

        final var random = new Random();

        if (random.nextBoolean()) {
            stressTestPathsRandomReadWrite();
        } else {
            stressTestResourceIdsRandomReadWrite();
        }

    }

    @Test(threadPoolSize = 100, invocationCount = 50)
    public void stressTestPathsRead() {
        stressTestPaths(weakReferenceLockSetService::getPathReadMonitor);
    }

    @Test(threadPoolSize = 100, invocationCount = 50)
    public void stressTestPathsWrite() {
        stressTestPaths(weakReferenceLockSetService::getPathWriteMonitor);
    }

    @Test(threadPoolSize = 100, invocationCount = 50)
    public void stressTestPathsRandomReadWrite() {

        final var random = new Random();

        stressTestPaths(random.nextBoolean()
                ? weakReferenceLockSetService::getPathReadMonitor
                : weakReferenceLockSetService::getPathWriteMonitor
        );

    }

    private void stressTestPaths(final Function<SortedSet<Path>, Monitor> monitorResolver) {

        final var random = new Random();
        final var count = MIN_PATH_COUNT + random.nextInt(MAX_PATH_COUNT);

        final var paths = random.ints(count, 0, testPaths.size())
                .mapToObj(index -> testPaths.get(index))
                .collect(toCollection(() -> new TreeSet<>(Paths.WILDCARD_FIRST)));

        final long then = System.nanoTime();

        try (final var monitor = monitorResolver.apply(paths)) {
            final long now = System.nanoTime();
            logger.trace("Entered scope in {} ns", then - now);
            simulateIoWorkload();
        }

    }

    @Test(threadPoolSize = 100, invocationCount = 10000)
    public void stressTestResourceIdsRead() {
        stressTestResourceIds(weakReferenceLockSetService::getResourceIdReadMonitor);
    }

    @Test(threadPoolSize = 100, invocationCount = 10000)
    public void stressTestResourceIdsWrite() {
        stressTestResourceIds(weakReferenceLockSetService::getResourceIdWriteMonitor);
    }

    @Test(threadPoolSize = 1000, invocationCount = 500)
    public void stressTestResourceIdsRandomReadWrite() {

        final var random = new Random();

        stressTestResourceIds(random.nextBoolean()
                ? weakReferenceLockSetService::getResourceIdReadMonitor
                : weakReferenceLockSetService::getResourceIdWriteMonitor
        );

    }

    private void stressTestResourceIds(final Function<SortedSet<ResourceId>, Monitor> monitorResolver) {

        final var random = new Random();
        final var count = MIN_RESOURCE_ID_COUNT + random.nextInt(MAX_RESOURCE_ID_COUNT);

        final var resourceIds = random.ints(count, 0, testResourceIds.size())
                .mapToObj(index -> testResourceIds.get(index))
                .collect(toCollection(TreeSet::new));

        final long then = System.nanoTime();

        try (final var monitor = monitorResolver.apply(resourceIds)) {
            final long now = System.nanoTime();
            logger.trace("Entered scope in {} ns", then - now);
            simulateIoWorkload();
        }

    }

    public void simulateIoWorkload() {
        final var random = new Random();
        random.ints(5, 512, 4096)
                .mapToObj(fileSize -> {

                    final var file = temporaryFiles.createTempFile();
                    logger.trace("Writing temporary file {}", file);

                    try (final var fc = FileChannel.open(file, WRITE)) {

                        final var contents = new byte[fileSize];
                        random.nextBytes(contents);

                        final var buffer = ByteBuffer.wrap(contents);

                        while (buffer.hasRemaining()) {
                            fc.write(buffer);
                        }

                        return file;

                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }

                })
                .forEach(file -> {
                    try {
                        logger.trace("Deleting temporary file {}", file);
                        Files.delete(file);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
    }

    @AfterClass
    public void afterTest() {
        weakReferenceLockSetService.logStatus();
    }

}
