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

    public static final String MAVEN_EXECUTABLE;

    public static final String MAVEN_EXECUTABLE_ENV = "MAVEN_EXECUTABLE";

    public static final String MAVEN_EXECUTABLE_PROPERTY = "dev.getelements.elements.mvn.sdk.local.executable";

    static {

        MAVEN_EXECUTABLE = System.getenv(MAVEN_EXECUTABLE_ENV) != null
                ? System.getenv(MAVEN_EXECUTABLE_ENV)
                : System.getProperty(MAVEN_EXECUTABLE_PROPERTY, "mvn");

        if (MAVEN_EXECUTABLE.isBlank()) {

            logger.error(
                    "Maven executable is not set. Set the environment variable '{}' or the system property '{}'.",
                    MAVEN_EXECUTABLE_ENV,
                    MAVEN_EXECUTABLE_PROPERTY
            );

            throw new SdkException("Maven executable is required but not found.");

        }

        logger.info("Using Maven executable: {}", MAVEN_EXECUTABLE);
        mvn("--version");

    }

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

        final int exit;

        try {

            final var command = Stream.concat(Stream.of(MAVEN_EXECUTABLE), Stream.of(args)).toArray(String[]::new);
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

            proc.waitFor();
            exit = proc.exitValue();

        } catch (final Exception ex) {
            logger.warn("An error occurred while executing Maven copy:dependencies", ex);
            throw new SdkException("Failed to execute Maven.", ex);
        }

        if (exit != 0) {
            logger.warn("Maven {} exited with code {}", exit, String.join(" ", args));
            throw new SdkException("Maven failed with exit code " + exit);
        }

    }

}

