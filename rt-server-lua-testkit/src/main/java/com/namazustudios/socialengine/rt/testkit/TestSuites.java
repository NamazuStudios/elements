package com.namazustudios.socialengine.rt.testkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Class used for building and parsing test suite files.
 */
public class TestSuites {

    private static final Logger logger = LoggerFactory.getLogger(TestSuites.class);

    private TestSuites() {}

    /**
     * Parses one or more test suite files based on their file names.
     *
     * @param testFileNames the names
     *
     * @return a {@link Stream<String>} containing all tests to run.
     */
    public static Stream<String> parseTestFiles(final Collection<String> testFileNames) {

        return testFileNames
                .stream()
                .flatMap(fileName -> {


                    final BufferedReader bufferedReader;

                    try {
                        bufferedReader = open(fileName);
                    } catch (FileNotFoundException e) {
                        return Stream.empty();
                    }

                    return bufferedReader.lines().onClose(() -> {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            logger.error("Caught error reading file '{}'.  Skipping.", e);
                        }
                    });

                })
                .filter(line -> !line.trim().startsWith("--"));
    }

    private static BufferedReader open(final String fileName) throws FileNotFoundException {

        final File file = new File(fileName);

        try {
            return new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            logger.error("File not found or could not read file {}.  Skipping.", fileName);
            throw ex;
        }

    }

}
