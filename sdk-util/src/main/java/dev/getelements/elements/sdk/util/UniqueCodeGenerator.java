package dev.getelements.elements.sdk.util;

import java.nio.CharBuffer;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Generates unique codes based on a set of candidate characters, a random generator, and a rejection predicate. The
 * generator will continue to produce codes until one is found that does not match the rejection predicate.
 */
public class UniqueCodeGenerator {

    private final Configuration configuration;

    /**
     * Creates a new UniqueCodeGenerator with the specified configuration.
     * @param configuration the configuration for the generator
     */
    public UniqueCodeGenerator(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Generates a unique code of the specified length, ensuring it does not match the rejection predicate.
     *
     * @param length the length of the code to generate
     */
    public Optional<String> tryGenerateUniqueCode(final int length) {
        return tryGenerateUniqueCode(length, configuration.defaultMaxAttempts);
    }

    /**
     * Generates a unique code of the specified length, ensuring it does not match the rejection predicate.
     *
     * @param length the length of the code to generate
     * @param maxAttempts the maximum number of attempts to generate a unique code
     */
    public Optional<String> tryGenerateUniqueCode(final int length, final int maxAttempts) {
        return tryGenerateUniqueCode(length, maxAttempts, s -> true);
    }

    /**
     * Generates a unique code of the specified length, ensuring it does not match the rejection predicate.
     *
     * @param length the length of the code to generate
     * @param maxAttempts the maximum number of attempts to generate a unique code
     * @param accept an additional predicate to accept generated codes after all tests have been attempted. If the
     *               predicate returns true, then the code is accepted; otherwise, generation continues until
     *               maxAttempts is reached.
     */
    public Optional<String> tryGenerateUniqueCode(final int length,
                                                  final int maxAttempts,
                                                  final Predicate<String> accept) {

        int attempts = 0;
        String candidate;

        final var reject = configuration.rejection.or(accept.negate());

        do {
            candidate = generateCandidateCode(length);
        } while (reject.test(candidate) && (++attempts < maxAttempts));

        return attempts < maxAttempts
                ? Optional.of(candidate)
                : Optional.empty();

    }

    /**
     * Generates a unique code of the specified length allowing for a custom result function to determine acceptance.
     * This is useful if generating a code requires additional processing, such as checking a database for uniqueness
     * and doing so while honoring concurrency concerns (such as atomic inserting or rejecting the code to prevent
     * duplicates).
     *
     * @param length the length of the code to generate
     * @param maxAttempts the maximum number of attempts to generate a unique code
     * @param resultFunction a function that tests the generated code and returns an Optional result. If the function
     *                       returns an Optional containing a value, then the code is accepted and the value is
     *                       returned. If the function returns an empty Optional, then generation continues until
     *                       maxAttempts is reached or a code is accepted.
     */
    public <T> Optional<T> tryComputeWithUniqueCode(final int length,
                                                    final int maxAttempts,
                                                    final Function<String, Optional<T>> resultFunction) {

        int attempts = 0;

        String code;
        Optional<T> candidate;

        do {
            code = generateCandidateCode(length);
            candidate = resultFunction.apply(code);
        } while (configuration.rejection.test(code) && candidate.isEmpty() && (++attempts < maxAttempts));

        return candidate;

    }

    private String generateCandidateCode(final int length) {

        final var code = CharBuffer.allocate(length);

        while (code.hasRemaining()) {
            final var i = configuration.random().nextInt(configuration.candidates.length());
            final var c = configuration.candidates().charAt(i);
            code.put(c);
        }

        return code.flip().toString();

    }

    /**
     * Configuration for the UniqueCodeGenerator.
     *
     * @param candidates the candidate characters to use for code generation
     * @param random the random generator
     * @param rejection the predicate to reject generated codes
     */
    public record Configuration(CharSequence candidates,
                                Random random,
                                int defaultMaxAttempts,
                                Predicate<String> rejection) {}

    /**
     * Builder for the UniqueCodeGenerator.Configuration.
     */
    public static class Builder {

        private Supplier<Random> random = Random::new;

        private Predicate<String> rejection = s -> false;

        private CharSequence candidates = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        private int defaultMaxAttempts = Integer.MAX_VALUE;

        /**
         * Specifies rejection criteria for generated codes.
         *
         * @param rejection the rejection predicate
         * @return this builder
         */
        public Builder rejecting(final Predicate<String> rejection) {
            this.rejection = this.rejection.or(rejection);
            return this;
        }

        /**
         * Rejects generated codes that contain substrings matching the provided rejection predicate.
         *
         * @param rejection the substring rejection predicate
         * @return this builder
         */
        public Builder rejectingSubstrings(final Predicate<String> rejection) {
            return rejecting(code -> CharSequenceStreams
                    .allSubSequences(code)
                    .map(CharSequence::toString)
                    .anyMatch(rejection)
            );
        }

        /**
         * Rejects the offensive words provided by the {@link OffensiveWordFilter}.
         *
         * @param offensiveWords the offensive word filter
         * @return this builder
         */
        public Builder rejectingOffensiveWords(final OffensiveWordFilter offensiveWords) {
            return rejecting(offensiveWords::isOffensive);
        }

        /**
         * Specifies a supplier for the {@link Random} to use for code generation.
         *
         * @param randomSupplier the random supplier
         * @return this builder
         */
        public Builder withRandomSupplier(final Supplier<Random> randomSupplier) {
            this.random = randomSupplier;
            return this;
        }

        /**
         * Specifies the {@link Random} to use for code generation.
         *
         * @param random the random generator
         * @return this builder
         */
        public Builder withRandom(final Random random) {
            return withRandomSupplier(() -> random);
        }

        /**
         * Specifies to use a {@link java.security.SecureRandom} for code generation.
         *
         * @return this builder
         */
        public Builder withSecureRandom() {
            return withRandomSupplier(java.security.SecureRandom::new);
        }

        /**
         * Specifies the candidate characters to use for code generation.
         * @param candidates the candidate characters
         * @return this builder
         */
        public Builder withCandidates(final CharSequence candidates) {
            this.candidates = candidates;
            return this;
        }

        /**
         * Specifies the default maximum number of attempts to generate a unique code.
         *
         * @param defaultMaxAttempts the default maximum attempts
         * @return this builder
         */
        public Builder withDefaultMaxAttempts(final int defaultMaxAttempts) {
            this.defaultMaxAttempts = defaultMaxAttempts;
            return this;
        }

        /**
         * Builds the UniqueCodeGenerator with the specified configuration.
         * @return the UniqueCodeGenerator
         */
        public UniqueCodeGenerator build() {
            return new UniqueCodeGenerator(
                    new Configuration(
                            candidates,
                            random.get(),
                            defaultMaxAttempts,
                            rejection
                    )
            );
        }

    }

}
