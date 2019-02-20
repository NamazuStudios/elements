package com.namazustudios.socialengine.model;


import java.util.ArrayList;
import java.util.List;

public interface Taggable {

    List<String> getTags();

    void setTags(List<String> tags);

    default void addTag(String tag) {
        final List<String> tags;
        if (getTags() != null) {
            tags = getTags();
        }
        else {
            tags = new ArrayList<>();
        }

        final String validatedTag = buildValidatedTag(tag);

        tags.add(validatedTag);

        setTags(tags);
    }

    /**
     * Returns a validated version of the given tag string, ensuring that the string is:
     *
     * • trimmed of whitespace and newlines
     * • excised of any newline chars from inside the string
     * • excised of any contiguous sequence of whitespace chars, replacing the sequence with a single underscore
     * character.
     *
     * If the resultant String would be the empty string, then we return null instead to signify that the given String
     * could not be validated.
     *
     * @param tag
     * @return the validated tag if valid input, or null if invalid input.
     */
    static String buildValidatedTag(final String tag) {
        if (tag == null) {
            return null;
        }

        final String validatedTag = tag
                .trim() // remove whitespace (and newlines) from the ends
                .replaceAll("[\n\r]", "")   // remove all newline chars from inside the string
                .replaceAll("\\s+", "_");   // replace any contiguous sequence of whitespace chars with a single underscore

        if (validatedTag.length() == 0) {
            return null;
        }

        return validatedTag;
    }

    /**
     * Takes in a list of tags and returns a list of validated tag strings.
     *
     * Note that the outputted list may be smaller so as not to include invalid tags, i.e.
     * outputList.size() <= inputList.size() is always true.
     *
     * @param tags
     * @return the list of validated tags.
     */
    static List<String> buildValidatedTags(final List<String> tags) {
        final List<String> validatedTags = new ArrayList<>();
        for (final String tag : tags) {
            final String validatedTag = buildValidatedTag(tag);

            if (validatedTag != null) {
                validatedTags.add(validatedTag);
            }
        }

        return validatedTags;
    }
}
