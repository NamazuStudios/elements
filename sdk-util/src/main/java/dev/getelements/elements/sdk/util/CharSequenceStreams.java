package dev.getelements.elements.sdk.util;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class for generating streams of CharSequence subsequences.
 */
public class CharSequenceStreams {

    private CharSequenceStreams() {}

    /**
     * Generates a stream of all possible sub-{@link CharSequence}s of the given {@link CharSequence}.
     *
     * @param sequence the input CharSequence
     * @return a Stream of CharSequence representing all possible subsequences
     */
    public static Stream<CharSequence> allSubSequences(final CharSequence sequence) {
        final int length = sequence.length();
        return IntStream
                .range(0, length)
                .boxed()
                .flatMap(start -> IntStream
                        .range(start + 1, length + 1)
                        .mapToObj(end -> sequence.subSequence(start, end))
                );
    }

}
