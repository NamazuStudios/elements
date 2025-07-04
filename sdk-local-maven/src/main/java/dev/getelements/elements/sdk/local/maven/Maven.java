package dev.getelements.elements.sdk.local.maven;

import dev.getelements.elements.sdk.exception.SdkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Maven {

    private static final Logger logger = LoggerFactory.getLogger(Maven.class);

    private static void check() {

        final var pom = Path.of("pom.xml");

        if (!Files.exists(pom)) {

            logger.error(
                    "No POM exists at '{}'. Check that you are running from the project directory.",
                    pom.toAbsolutePath()
            );

            throw new SdkException("Maven pom.xml file is required but not found.");

        }

    }

    /**
     * Runs Maven with the specified arguments.
     *
     * @param args the arguments
     */
    public static void mvn(final String ... args) {

        check();

        try {

            final var command = Stream.concat(Stream.of("mvn"), Stream.of(args)).toArray(String[]::new);
            final var pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            // Start the process
            final var proc = pb.start();

            // Log Maven output
            try (final var is = proc.getInputStream();
                 final var isr = new InputStreamReader(is);
                 final var reader = new BufferedReader(isr)) {

                String line;

                while ((line = reader.readLine()) != null) {
                    logger.info("{}", line);
                }

            }

            // Wait for the process to complete
            int exitCode = proc.waitFor();

            // Check exit code
            if (exitCode != 0) {
                logger.warn("Maven exited with code {}", exitCode);
                throw new SdkException("Maven copy:dependencies failed with exit code " + exitCode);
            }

        } catch (final Exception ex) {
            logger.warn("An error occurred while executing Maven copy:dependencies", ex);
            throw new SdkException("Failed to execute Maven copy:dependencies", ex);
        }

    }

}

