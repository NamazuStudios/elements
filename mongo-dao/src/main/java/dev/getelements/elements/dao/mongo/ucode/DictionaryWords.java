package dev.getelements.elements.dao.mongo.ucode;

import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;
import java.util.Set;

/**
 * Dummy class to hold dictionary words for unique code generation. We exclude all words that are dictionary words
 * to ensure that generated unique codes are strictly codes and not actual words.
 */
@Singleton
public class DictionaryWords {

    public static final String DICTIONARY_RESOURCE_PATH = "/dictionary_words.properties";

    private final Set<String> words;

    /**
     * Creates a new DictionaryWords instance and loads the dictionary words from the resource file.
     */
    public DictionaryWords() {
        final var properties = new Properties();

        try (var is = DictionaryWords.class.getResourceAsStream(DICTIONARY_RESOURCE_PATH)) {
            properties.load(is);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        this.words = properties.stringPropertyNames();
    }

    /**
     * Checks if the given word is a dictionary word.
     *
     * @param word the word
     * @return true if the word is a dictionary word, false otherwise
     */
    public boolean isDictionaryWord(final String word) {
        return words.contains(word);
    }

}
