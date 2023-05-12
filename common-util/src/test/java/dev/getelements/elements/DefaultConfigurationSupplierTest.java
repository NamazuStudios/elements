package dev.getelements.elements;

import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.rt.util.TemporaryFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.isExecutable;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class DefaultConfigurationSupplierTest {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(DefaultConfigurationSupplierTest.class);

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurationSupplierTest.class);

    @Test
    public void testPropertiesAccumulateProperly() throws IOException {

        final var paths = new ArrayList<Path>();
        final var expected = new Properties();

        for (int i = 0; i < 10; ++i) {

            final var properties = new Properties();

            for (char c = 'a'; c <= 'z'; ++c) {
                final var key = format("%c.key.%d", c, i);
                final var val = format("%c.val.%d", c, i);
                expected.put(key, val);
                properties.put(key, val);
            }

            final var tmp = temporaryFiles.createTempFile(".properties");

            try (var fos = new FileOutputStream(tmp.toFile())) {
                properties.store(fos, format("Test Iteration %d", i));
            }

            paths.add(tmp);

        }

        final var actual = DefaultConfigurationSupplier.loadProperties(new Properties(), paths.toArray(new Path[0]));
        assertEquals(actual, expected);

        paths.forEach(path -> {
            try {
                deleteIfExists(path);
            } catch (IOException ex) {
                logger.error("Caught exception deleting temp file.", ex);
            }
        });

    }

    @Test
    public void testRemapEnvironment() throws Exception {

        final var props = System.getProperties();

        final var javaHome = Paths.get(props.get("java.home").toString());
        final var classpath = props.get("java.class.path").toString();

        var jvmExecutable = javaHome.resolve("bin/java");

        if (!isExecutable(jvmExecutable)) {
            jvmExecutable = javaHome.resolve("bin/java.exe");
        }

        if (!isExecutable(jvmExecutable)) {
            fail("Unable to determine JVM executable: " + jvmExecutable);
        }

        final var processBuilder = new ProcessBuilder();
        processBuilder.environment().put("CLASSPATH", classpath);
        processBuilder.environment().put("dev.getelements.test.dot", "dot");
        processBuilder.environment().put("com_namazustudios_test_underscore", "underscore");
        processBuilder.environment().put("ELEMENTS_ENVIRONMENT_UPPERCASE", "uppercase");
        processBuilder.command(jvmExecutable.toString(), TestExecutable.class.getName());

        final var process = processBuilder.start();
        final var result = process.waitFor();

        final var stdout = new String(process.getInputStream().readAllBytes());
        assertEquals(result, 0, "Process output: " + stdout);

    }

    public static class TestExecutable {

        public static void main(final String[] args) {

            final var properties = DefaultConfigurationSupplier.loadProperties();
            final var dot = properties.get("dev.getelements.test.dot");
            final var underscore = properties.get("dev.getelements.test.underscore");
            final var uppercase = properties.get("ELEMENTS_ENVIRONMENT_UPPERCASE");

            assertEquals(dot, "dot");
            assertEquals(underscore, "underscore");
            assertEquals(uppercase, "uppercase");

        }

    }

}
