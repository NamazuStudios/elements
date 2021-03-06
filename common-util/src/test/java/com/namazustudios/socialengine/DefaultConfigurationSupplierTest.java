package com.namazustudios.socialengine;

import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;

import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static org.testng.Assert.assertEquals;

public class DefaultConfigurationSupplierTest {

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

            final var tmp = Files.createTempFile("DefaultConfigurationSupplierTest", ".properties");

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

}
