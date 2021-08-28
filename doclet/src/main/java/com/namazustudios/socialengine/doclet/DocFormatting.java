package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.Private;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Formatting column utilities.
 */
@Private
public class DocFormatting {

    private DocFormatting() {}

    private static final Pattern WHOLE_WORD_SPLIT = Pattern.compile("\\s+");

    /**
     * Splits a string into multiple lines with the max column count, specifying a prefix.
     *
     * @param input the input text
     * @param maxColumns the maximum number of columns
     * @param prefix the prefix string
     * @return a {@link List<String>}, with each element representing the line of text
     */
    public static List<String> split(final String input, final int maxColumns, final String prefix) {

        if (maxColumns <= prefix.length())
            throw new IllegalArgumentException("Columns must exceed prefix length + 1");

        final var result = new ArrayList<String>();

        final var line = new StringBuilder(prefix);
        final var words = WHOLE_WORD_SPLIT.splitAsStream(input).iterator();

        while (words.hasNext()) {

            final var word = words.next();
            final var willExceedLength = line.length() + word.length() + prefix.length() > maxColumns;

            if (willExceedLength) {
                result.add(line.toString());
                line.setLength(prefix.length());
            }

            line.append(word);

            if (line.length() < maxColumns) {
                line.append(" ");
            }

        }

        return result;

    }

}
