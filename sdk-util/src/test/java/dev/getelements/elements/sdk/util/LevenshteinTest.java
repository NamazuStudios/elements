package dev.getelements.elements.sdk.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class LevenshteinTest {

    @DataProvider(name = "distanceCases")
    public Object[][] distanceCases() {
        return new Object[][]{
                // Identity
                {"", "", 0},
                {"abc", "abc", 0},

                // Insertions
                {"cat", "cart", 1},
                {"a", "aaa", 2},

                // Deletions
                {"stone", "tone", 1},
                {"abcde", "abde", 1},

                // Substitutions
                {"kitten", "sitten", 1},
                {"gumbo", "gambol", 2},

                // Mixed operations
                {"kitten", "sitting", 3},
                {"flaw", "lawn", 2},

                // Empty source or target
                {"", "hello", 5},
                {"test", "", 4},

                // Asymmetry check (same distance either direction)
                {"abc", "yabd", 2},
                {"yabd", "abc", 2}
        };
    }

    @Test(dataProvider = "distanceCases")
    public void testLevenshteinDistance(String source, String target, int expected) {
        int actual = Levenshtein.distance(source, target);
        assertEquals(actual, expected,
                "Unexpected distance for [" + source + "] to [" + target + "]");
    }

    @Test
    public void testLargeInputs() {
        String a = "a".repeat(500);
        String b = "a".repeat(499) + "b"; // one substitution at the end

        int result = Levenshtein.distance(a, b);
        assertEquals(result, 1);
    }

}
