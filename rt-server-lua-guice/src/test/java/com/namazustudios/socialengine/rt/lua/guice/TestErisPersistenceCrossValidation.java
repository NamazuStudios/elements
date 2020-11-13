package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.assertEquals;

@Guice(modules = ErisPersistenceTestModule.class)
public class TestErisPersistenceCrossValidation {

    private static final Logger logger = LoggerFactory.getLogger(TestErisPersistenceCrossValidation.class);

    private static TestTemporaryFiles testTemporaryFiles = new TestTemporaryFiles();

    private ResourceLoader resourceLoader;

    @AfterSuite
    public static void deleteTempFiles() {
        testTemporaryFiles.deleteTempFiles();
    }

    @DataProvider
    public static Object[][] allLuaResources() {
        return TestCoreErisPersistenceStreams.allLuaResources();
    }

    private final List<Object[]> intermediates = new ArrayList<>();

    @Test(dataProvider = "allLuaResources", invocationCount = 10)
    public void testStreamAndFileProduceIdenticalOutput(final String moduleName) throws IOException {

        logger.debug("Testing Persistence for {}", moduleName);

        final var streamFile = testTemporaryFiles.createTempFile();
        final var channelFile = testTemporaryFiles.createTempFile();

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

    @Test(dataProvider = "intermediateFiles", dependsOnMethods = "testStreamAndFileProduceIdenticalOutput", invocationCount = 10)
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

        logger.info("Loading file {} written by {} using ReadableByteChannel", file, source);

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
