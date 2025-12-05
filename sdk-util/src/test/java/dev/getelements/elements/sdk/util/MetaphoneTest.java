package dev.getelements.elements.sdk.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MetaphoneTest {

    @Test
    public void testBasicEquivalence() {
        Assert.assertEquals(
                Metaphone.metaphone("Smith"),
                Metaphone.metaphone("Smyth"),
                "Smith and Smyth should have equal metaphone codes"
        );

        Assert.assertEquals(
                Metaphone.metaphone("Steven"),
                Metaphone.metaphone("Stephen"),
                "Steven and Stephen should map to same code"
        );
    }

    @Test
    public void testInitialTransformations() {
        Assert.assertEquals(Metaphone.metaphone("Knight"), "NT".substring(0, 2)); // KN -> N
        Assert.assertEquals(Metaphone.metaphone("Xavier"), "SFR".substring(0, 3)); // X -> S at start
    }

    @Test
    public void testConsonantRules() {
        Assert.assertEquals(Metaphone.metaphone("Christopher"), Metaphone.metaphone("Kristopher"));
        Assert.assertEquals(Metaphone.metaphone("Django"), Metaphone.metaphone("Jango"));
    }

    @Test
    public void testVowelAndLength() {
        Assert.assertTrue(Metaphone.metaphone("Apple").startsWith("A"), "Vowel at start should be preserved");
        Assert.assertTrue(Metaphone.metaphone("Apple", 2).length() <= 2);
    }

    @Test
    public void testEdgeCases() {
        Assert.assertEquals(Metaphone.metaphone(""), "");
        Assert.assertEquals(Metaphone.metaphone("   "), "");
        Assert.assertEquals(Metaphone.metaphone(null), "");
        Assert.assertEquals(Metaphone.metaphone("123"), "");
    }

    @Test
    public void testKnownOutputs() {
        Assert.assertEquals(Metaphone.metaphone("Phone"), "FN");
        Assert.assertEquals(Metaphone.metaphone("Shell"), "XL"); // SH -> X
        Assert.assertEquals(Metaphone.metaphone("Chaos"), "KS"); // CH after S rule
    }

}
