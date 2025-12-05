package dev.getelements.elements.dao.mongo.ucode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;
import java.util.Set;

/**
 * Dummy class to hold dictionary words for unique code generation. We exclude all words that are dictionary words
 * to ensure that generated unique codes are strictly codes and not actual words.
 */
public class DictionaryWords {

    public static final String DICTIONARY_RESOURCE_PATH = "/dictionary_words.properties";

    private static final Set<String> DICTIONARY_WORDS;

    static {

        final var properties = new Properties();

        try (var is = DictionaryWords.class.getResourceAsStream(DICTIONARY_RESOURCE_PATH)) {
            properties.load(is);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        DICTIONARY_WORDS = properties.stringPropertyNames();

    }

    /**
     * Checks if the given word is a dictionary word.
     *
     * @param word the word
     * @return true if the word is a dictionary word, false otherwise
     */
    public static boolean isDictionaryWord(final String word) {
        return DICTIONARY_WORDS.contains(word);
    }

}
