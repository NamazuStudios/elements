package dev.getelements.elements.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

/**
 * Placeholder class for offensive word filtering functionality. This includes words that are considered offensive,
 * hateful, or would be inappropriate in automatically generated unique codes. This loads a list of such words from
 * a properties file and provides methods to check and filter them as needed.
 *
 * The line of the properties file should contain one offensive word per line, with the value of the property being
 * the permitted edit distance for that word. We provide a builtin set of bad words for English, but additional
 * languages and custom bad word lists can be provided by overriding the default properties file or suppying an
 * instance of {@link java.util.Properties} when initializing the filter.
 *
 * For example:
 * BADWORD=1 means "BADWORD" is offensive, and any word within an edit distance of 1 from "BADWORD" is also considered
 * offensive. This could include "BADWRD", "BADWOR", "BADWORDX", etc.
 *
 * The default properties file is included on the classpath and can be loaded using the Builder's
 * {@link Builder#addDefaultWords()} method. The built-in file contains a basic set of words tht are commonly
 * considered hate speed in English. In addition to hate speech, it includes words that while not exactly hate speed
 * could be considered vulgar, awkward, or inappropriate in certain contexts.
 *
 */
public class OffensiveWordFilter {

    private static final Logger logger = LoggerFactory.getLogger(OffensiveWordFilter.class);

    private final Configuration configuration;

    /**
     * Creates a new OffensiveWordFilter with the given configuration.
     *
     * @param configuration the configuration
     */
    public OffensiveWordFilter(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns true if the input contains any offensive words or their variants based on the defined edit distances.
     * @param input the input CharSequence to check
     * @return true if offensive words are found, false otherwise
     */
    public boolean isOffensive(final CharSequence input) {
        final var transformed = configuration.transform(input);
        return CharSequenceStreams
                .allSubSequences(transformed)
                .anyMatch(this::check);
    }

    private boolean check(final CharSequence input) {
        return configuration.words()
                .stream()
                .anyMatch(word -> word.matches(input));
    }

    /**
     * Represents an offensive word and its associated edit distance.
     *
     * @param word the word
     * @param distance the permitted edit distance
     */
    public record Word(CharSequence word, int distance) {

        public boolean matches(final CharSequence charSequence) {

            final var matches = Levenshtein.distance(word(), charSequence) <= distance;

            if (matches) {
                logger.debug("Input '{}' matches offensive word '{}' with distance {}",
                        charSequence,
                        word(),
                        distance()
                );
            }

            return matches;

        }

    }

    /**
     * Configuration for the OffensiveWordFilter.
     *
     * @param words the words
     * @param transform the transformation function to apply to input before checking
     */
    public record Configuration(List<Word> words, Function<CharSequence, CharSequence> transform) {

        public Configuration {
            words = words.stream()
                    .map(w -> new Word(transform.apply(w.word()), w.distance()))
                    .toList();
        }

        public CharSequence transform(final CharSequence charSequence) {
            return transform.apply(charSequence);
        }

    }

    /**
     * Builder for constructing an OffensiveWordFilter with a list of offensive words.
     */
    static class Builder {

        public static String DEFAULT_OFFENSIVE_WORDS_PATH = "/offensive_words.properties";

        private Function<CharSequence, CharSequence> transform = s -> s;

        private final List<Word> words = new ArrayList<>();

        /**
         * Adds a word to the offensive word list.
         * @param word the word
         *
         * @return the builder
         */
        public Builder addWord(final Word word) {
            words.add(word);
            return this;
        }

        /**
         * Adds words from a Properties object to the offensive word list.
         *
         * @param properties the properties containing words and their edit distances
         * @return the builder
         */
        public Builder addWords(final Properties properties) {

            for (var entry : properties.entrySet()) {
                final var key = (String) entry.getKey();
                final var distance = Integer.parseInt((String) entry.getValue());
                addWord(new Word(key, distance));
            }

            return this;

        }

        /**
         * Adds words from an InputStream containing properties to the offensive word list.
         *
         * @param stream the input stream
         * @return the builder
         * @throws IOException if an I/O error occurs
         */
        public Builder addWords(final InputStream stream) throws IOException {
            final Properties properties = new Properties();
            properties.load(stream);
            return addWords(properties);
        }

        /**
         * Adds the default offensive words from the built-in properties file.
         *
         * @return the builder
         */
        public Builder addDefaultWords() {

            try (final var is = OffensiveWordFilter.class.getResourceAsStream(DEFAULT_OFFENSIVE_WORDS_PATH)) {

                if (is == null) {
                    logger.error("No offensive words found on Classpath at {}", DEFAULT_OFFENSIVE_WORDS_PATH);
                }

                addWords(is);

            } catch (final IOException ex) {
                throw new UncheckedIOException(ex);
            }

            return this;

        }

        /**
         * Configures the filter to ignore case when checking for offensive words.
         *
         * @return the builder
         */
        public Builder ignoringCase() {
            transform = transform.andThen(in -> in.toString().toUpperCase());
            return this;
        }

        /**
         * Builds the OffensiveWordFilter instance.
         *
         * @return the OffensiveWordFilter
         */
        public OffensiveWordFilter build() {
            final var configuration = new Configuration(words, transform);
            return new OffensiveWordFilter(configuration);
        }

    }

}
