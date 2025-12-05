package dev.getelements.elements.dao.mongo.ucode;

import dev.getelements.elements.sdk.util.OffensiveWordFilter;

import java.security.SecureRandom;

public class UniqueCodeGenerator {

    /**
     * Filter to exclude offensive words from generated codes.
     */
    private static final OffensiveWordFilter OFFENSIVE_WORD_FILTER = new OffensiveWordFilter.Builder()
            .addDefaultWords()
            .ignoringCase()
            .build();

    /**
     * Characters used for code generation (excluding ambiguous characters like I, O, 0, 1).
     */
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";

    /**
     * Secure random number generator for code generation.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


}
