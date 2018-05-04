package com.namazustudios.socialengine.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

/**
 * Generates display names from a list of the 100 most popular American baby names for boys and girls.  The name is
 * randomly selected with a randomly assigned last initial.  For example, "Jon S."
 */
public class SimpleDisplayNameGenerator implements DisplayNameGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SimpleDisplayNameGenerator.class);

    private static final List<String> FIRST_NAMES;

    private static final List<String> LAST_INITIALS;

    static {

        final List<String> names = new ArrayList<>();

        try {
            try (final InputStream is = SimpleDisplayNameGenerator.class.getResourceAsStream("/test-display-names.txt");
                 final BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                String line = reader.readLine();

                while (line != null) {
                    names.add(line);
                    line = reader.readLine();
                }

            }
        } catch (IOException e) {
            logger.warn("Could not load name list.");
        }

        final List<String> lastInitials = IntStream.range('A', 'Z')
                .mapToObj(i -> (char)i + ".")
                .collect(Collectors.toList());

        FIRST_NAMES = unmodifiableList(names);
        LAST_INITIALS = unmodifiableList(lastInitials);

    }

    @Override
    public String generate() {
        final Random random = ThreadLocalRandom.current();
        final int firstNameIndex = random.nextInt(FIRST_NAMES.size());
        final int lastInitialIndex = random.nextInt(LAST_INITIALS.size());
        return format("%s %s", FIRST_NAMES.get(firstNameIndex), LAST_INITIALS.get(lastInitialIndex));
    }

}
