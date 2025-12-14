package dev.getelements.elements.sdk.util;

import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

import static org.testng.Assert.assertTrue;

public class OffensiveWordFilterTest {

    private static final int BENCHMARK_CODE_LENGTH = 4;

    private static final int RANDOM_SAMPLE_SET = 50000;

    private static final double ACCEPTABLE_REJECTION_PERCENTAGE = 0.20;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final Deque<String> offensive = new ConcurrentLinkedDeque<>();

    private final Deque<String> inoffensive = new ConcurrentLinkedDeque<>();

    private OffensiveWordFilter filter = new OffensiveWordFilter.Builder()
            .addDefaultWords()
            .ignoringCase()
            .addDefaultWords()
            .build();

    @DataProvider
    public Object[][] offensiveWords() {
        return filter.getConfiguration()
                .words()
                .stream()
                .map(word -> new Object[]{word})
                .toArray(Object[][]::new);
    }

    @DataProvider(parallel = true)
    public Object[][] allFourLetterCodes() {

        final var codes = new TreeSet<String>();
        final var random = new SecureRandom();

        final var generator = new UniqueCodeGenerator.Builder()
                .rejecting(codes::contains)
                .withCandidates(CHARACTERS)
                .withRandom(random)
                .build();

        for (int i = 0; i < RANDOM_SAMPLE_SET; i++) {
            final var code = generator.tryGenerateUniqueCode(BENCHMARK_CODE_LENGTH);
            codes.add(code.orElseThrow(IllegalStateException::new));
        }

        return codes
                .stream()
                .map(code -> new Object[]{code})
                .toArray(Object[][]::new);

    }

    @Test(dataProvider = "offensiveWords")
    public void testOffensiveWordsAreFiltered(final OffensiveWordFilter.Word word) {
        for (int i = 0; i <= word.distance(); ++i) {
            final var edited = editToDistance(word.word().toString(), i);
            assertTrue(filter.isOffensive(edited),"Expected " + edited + " to be offensive.");
        }
    }


    private String editToDistance(final String source, final int distance) {

        String candidate = source;

        while (Levenshtein.distance(source, candidate) < distance) {
            candidate = getRandomEdit(candidate);
        }

        return candidate;

    }

    private String getRandomEdit(final String source) {

        final var random = ThreadLocalRandom.current();

        final var index = random.nextInt(source.length());
        final var editToApply = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));

        return switch (random.nextInt(3)) {
            case 0 -> Levenshtein.insertAt(source, index, editToApply);
            case 1 -> Levenshtein.substituteAt(source, index, editToApply);
            case 2 -> Levenshtein.deleteAt(source, index);
            default -> throw new IllegalStateException("Unexpected value: " + random);
        };

    }

    @Test(dataProvider = "allFourLetterCodes")
    public void testOffensiveWordsBenchmark(final String code) {

        if (filter.isOffensive(code)) {
            offensive.add(code);
        } else {
            inoffensive.add(code);
        }

    }

    @Test(dependsOnMethods = "testOffensiveWordsBenchmark")
    public void checkOffensiveWordsAvailability() {
        final double offensiveCount = offensive.size();
        final double inoffensiveCount = inoffensive.size();
        final double rejectionPercentage = offensiveCount / (inoffensiveCount + offensiveCount);
        assertTrue(rejectionPercentage < ACCEPTABLE_REJECTION_PERCENTAGE,
            "Offensive Word Filter is Too Strict: %f > %f".formatted(
                    rejectionPercentage,
                    ACCEPTABLE_REJECTION_PERCENTAGE)
        );
    }

    @AfterClass
    public void writeResults() throws IOException {

        try (final var fos = new FileOutputStream("offensive_words.txt");
             final var writer = new PrintWriter(fos)) {

            for (final var word : offensive) {
                writer.println(word);
            }

        }

        try (final var fos = new FileOutputStream("inoffensive_words.txt");
             final var writer = new PrintWriter(fos)) {

            for (final var word : inoffensive) {
                writer.println(word);
            }

        }

    }

}
