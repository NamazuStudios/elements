package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.util.TemporaryFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.inject.Guice.createInjector;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertEquals;

public class TestErisPersistenceCrossValidation {

    @Factory
    public static Object[] getIntegrationTests() {

        final var injector = createInjector(new ErisPersistenceTestModule());

        return new Object[] {
                injector.getInstance(TestErisPersistenceCrossValidation.class)
        };

    }

    private static final Logger logger = LoggerFactory.getLogger(TestErisPersistenceCrossValidation.class);

    private static TemporaryFiles temporaryFiles = new TemporaryFiles(TestErisPersistenceCrossValidation.class);

    private ResourceLoader resourceLoader;

    @AfterSuite
    public static void deleteTempFiles() {
        temporaryFiles.deleteTempFilesAndDirectories();
    }

    @DataProvider
    public static Object[][] allLuaResources() {
        return TestCoreErisPersistenceStreams.allLuaResources();
    }

    private final List<Object[]> intermediates = new ArrayList<>();

    @Test(dataProvider = "allLuaResources", invocationCount = 2)
    public void testStreamAndFileProduceIdenticalOutput(final String moduleName) throws IOException {

        logger.debug("Testing Persistence for {}", moduleName);

        final var streamFile = temporaryFiles.createTempFile();
        final var channelFile = temporaryFiles.createTempFile();

        try (final var fos = new FileOutputStream(streamFile.toFile());
             final var bos = new BufferedOutputStream(fos);
             final var resource = getResourceLoader().load(moduleName)) {
            resource.setVerbose(true);
            resource.serialize(bos);
        }

        try (final var wbc = open(channelFile, WRITE);
             final var resource = getResourceLoader().load(moduleName)) {
            resource.setVerbose(true);
            resource.serialize(wbc);
        }

        try (final var sfc = open(streamFile, READ);
             final var cfc = open(channelFile, READ)) {

            assertEquals(sfc.size(), cfc.size());

            final var smbb = sfc.map(READ_ONLY, 0, sfc.size());
            final var cmbb = sfc.map(READ_ONLY, 0, cfc.size());
            assertEquals(smbb, cmbb);

        }

        intermediates.add(new Object[]{"stream", streamFile, true});
        intermediates.add(new Object[]{"stream", streamFile, false});
        intermediates.add(new Object[]{"channel", channelFile, true});
        intermediates.add(new Object[]{"channel", channelFile, false});

    }

    @DataProvider
    public Object[][] intermediateFiles() {
        return intermediates.toArray(Object[][]::new);
    }

    @Test(dataProvider = "intermediateFiles", dependsOnMethods = "testStreamAndFileProduceIdenticalOutput", invocationCount = 2)
    public void testReadStream(final String source, final Path file, final boolean verbose) throws IOException {

        logger.debug("Loading file {} written by {} using InputStream", file, source);

        try (final var fis = new FileInputStream(file.toFile());
             final var bis = new BufferedInputStream(fis);
             final var resource = getResourceLoader().load(bis, verbose)) {
            logger.debug("Loaded {} file {} (verbose: {})", resource.getId(), file, verbose);
        }

    }

    @Test(dataProvider = "intermediateFiles", dependsOnMethods = "testStreamAndFileProduceIdenticalOutput", invocationCount = 10)
    public void testReadChannel(final String source, final Path file, final boolean verbose) throws IOException {

        logger.debug("Loading file {} written by {} using ReadableByteChannel", file, source);

        try (final var rbc = open(file, READ);
             final var resource = getResourceLoader().load(rbc, verbose)) {
            logger.debug("Loaded {} file {} (verbose: {})", resource.getId(), file, verbose);
        }

    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
