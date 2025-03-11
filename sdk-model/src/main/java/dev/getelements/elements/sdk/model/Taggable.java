package dev.getelements.elements.sdk.model;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface Taggable {

    List<String> getTags();

    void setTags(List<String> tags);

    /**
     * Adds a tag without performing validation of the tag string or the list of strings.
     *
     * @param tag
     */
    default void addTag(final String tag) {
        if (tag == null) {
            return;
        }

        final List<String> tags;

        if (getTags() != null) {
            tags = getTags();
        }
        else {
            tags = new ArrayList<>();
        }

        tags.add(tag);
        setTags(tags);
    }

    /**
     * In-place validation and replacement of tags.
     */
    default void validateTags() {
        List<String> validatedTags = buildValidatedTags(getTags());
        setTags(validatedTags);
    }

    /**
     * Validates the given list of tags and then sets them.
     *
     * @param tags
     */
    default void validateAndSetTags(List<String> tags) {
        List<String> validatedTags = buildValidatedTags(tags);
        setTags(validatedTags);
    }

    /**
     * Validates the given tag and adds it to the existing list of tags. If the tags is null, then a new ArrayList is
     * created as well.
     *
     * @param tag
     */
    default void validateAndAddTag(String tag) {
        final String validatedTag = buildValidatedTag(tag);

        if (validatedTag != null) {
            addTag(validatedTag);
        }
    }

    /**
     * Returns a validated version of the given tag string, ensuring that the string is:
     *
     * • trimmed of whitespace and newlines;
     * • excised of any newline chars from inside the string;
     * • excised of any contiguous sequence of whitespace chars, replacing the sequence with a single underscore
     * character;
     * • [TBD] excised of any non-alpha, non-digit characters, replacing them with underscores; and
     * • [TBD] forced to all lowercase.
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
                .trim() // remove whitespace/newlines from the ends
                .replaceAll("[^\\S ]+", "")   // remove all whitespace/newline chars (minus space char " ") from inside the string
                .replaceAll("\\s+", "_");   // replace any contiguous sequence of whitespace chars with a single underscore
                //.replaceAll("[^\\p{IsAlphabetic}^\\p{IsDigit}]", "_")   // only allow alphanumeric (incl. int'l)
                //.toLowerCase(); // and finally, make it lowercase

        if (validatedTag.length() == 0) {
            return null;
        }

        return validatedTag;
    }

    /**
     * Takes in a list of tags and returns a list of deduplicated, validated tag strings.
     *
     * Note that the outputted list may be smaller so as not to include invalid tags, i.e.
     * outputList.size() &lt;= inputList.size() is always true.
     *
     * @param tags the tags
     * @return the list of deduplicated, validated tags.
     */
    static List<String> buildValidatedTags(final List<String> tags) {
        if (tags == null) {
            return null;
        }

        final Set<String> validatedTagsSet = new LinkedHashSet<>();
        for (final String tag : tags) {
            final String validatedTag = buildValidatedTag(tag);

            if (validatedTag != null) {
                validatedTagsSet.add(validatedTag);
            }
        }

        final List<String> validatedTags = new ArrayList<>(validatedTagsSet);

        return validatedTags;
    }
}
