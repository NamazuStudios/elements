package com.namazustudios.socialengine.jnlua;

import com.sun.jna.NativeLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;

/**
 * Attempts to laod the default lua libraries from the classpath.  This attempts a loading process similar to that of
 * of JNA.
 */
public class DefaultLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLoader.class);

    private static final DefaultLoader instance = new DefaultLoader();

    public static DefaultLoader getInstance() {
        return instance;
    }

    private static final String LIBRARY_TEMP_PREFIX = "native-lib";

    private final LibraryOS libraryOs;

    private final Path libraryPath;

    private DefaultLoader() {

        this.libraryOs = determineOS();

        try {
            this.libraryPath = Files.createTempDirectory(LIBRARY_TEMP_PREFIX);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create temporary directory.");
        }

    }

    private LibraryOS determineOS() {

        final String osName = System.getProperty("os.name");

        if (osName.startsWith("Linux")) {
            final String vmName = System.getProperty("java.vm.name").toLowerCase();
            return "dalvik".equals(vmName) ? LibraryOS.UNSPECIFIED : LibraryOS.LINUX;
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            return LibraryOS.MAC;
        } else {
            return LibraryOS.UNSPECIFIED;
        }

    }

    @Override
    public void load() {
        NativeLibrary.getInstance("lua53");
        NativeLibrary.getInstance("jnlua");
        load("jnlua");
    }

    private void load(final String libraryName) {

        logger.debug("Attempting to load native library {}", libraryName);

        final String libraryFilename = getOS().getLibraryFilename(libraryName);
        final String libraryClasspath = getOS().getLibraryFullClasspath(libraryName);

        logger.debug("Loading {} native library from {} ", libraryName, libraryClasspath);

        try (final InputStream is = DefaultLoader.class.getResourceAsStream(libraryClasspath)) {

            if (is == null) {
                throw new UnsatisfiedLinkError("No library '" + libraryName + "' on classpath.");
            }

            final Path destination = getLibraryPath().resolve(libraryFilename);
            Files.copy(is, destination);

            final File libraryFile = destination.toFile();
            libraryFile.deleteOnExit();
            System.load(libraryFile.getAbsolutePath());

            logger.info("Successfully loaded {} to {}", libraryName, libraryFile.getAbsolutePath());

        } catch (IOException ex) {
            logger.error("Failed to load library {}", libraryName, ex);
            throw new IllegalStateException("Unable to laod local library.");
        } catch (UnsatisfiedLinkError er) {
            logger.error("Failed to load library {}", libraryName, er);
            throw er;
        }

    }

    /**
     * Returns the currently running {@link LibraryOS}.
     *
     * @return the currently running LibraryOS.
     */
    public LibraryOS getOS() {
        return libraryOs;
    }

    /**
     * Gets the library path where all libraries will be copied before they are loaded.
     *
     * @return the library path.
     */
    public Path getLibraryPath() {
        return libraryPath;
    }


}
