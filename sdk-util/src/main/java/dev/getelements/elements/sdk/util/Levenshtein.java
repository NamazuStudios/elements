package dev.getelements.elements.sdk.util;

/**
 * Utility class for calculating the Levenshtein distance between two CharSequences.
 */
public class Levenshtein {

    private Levenshtein() {}

    /**
     * Calculates the Levenshtein distance between two CharSequences.
     * @param source the source CharSequence
     * @param target the target CharSequence
     * @return the Levenshtein distance
     */
    public static int distance(final CharSequence source, final CharSequence target) {

        final int lenA = source.length();
        final int lenB = target.length();

        // If one is empty, distance is the other length
        if (lenA == 0) return lenB;
        if (lenB == 0) return lenA;

        // Use two working rows to reduce memory use to O(min(n, m))
        int[] prev = new int[lenB + 1];
        int[] curr = new int[lenB + 1];

        // Initialize previous row
        for (int j = 0; j <= lenB; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= lenA; i++) {
            curr[0] = i;

            char ca = source.charAt(i - 1);

            for (int j = 1; j <= lenB; j++) {
                char cb = target.charAt(j - 1);

                int cost = (ca == cb) ? 0 : 1;

                curr[j] = Math.min(
                        Math.min(
                                prev[j] + 1,      // deletion
                                curr[j - 1] + 1   // insertion
                        ),
                        prev[j - 1] + cost  // substitution
                );
            }

            // Swap rows
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[lenB];

    }

}
