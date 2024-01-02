package dev.getelements.elements.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Optional;

import static dev.getelements.elements.util.PhoneNormalizer.normalizePhoneNb;


public class PhoneNormalizerTest {

    @Test
    public void shouldNormalizeCommonNumbers() {
        final String phone = "+17034567890";
        Assert.assertEquals(normalizePhoneNb("+17034567890"), Optional.of("+17034567890"));
        Assert.assertEquals(normalizePhoneNb("+13 034 567 890"), Optional.of("+13034567890"));
        Assert.assertEquals(normalizePhoneNb("0048782733456"), Optional.of("0048782733456"));
        Assert.assertEquals(normalizePhoneNb("0048 782-733-456"), Optional.of("0048782733456"));
    }

    @Test
    public void shouldNormalizeUnknownRegionNumber() {
        Assert.assertEquals(normalizePhoneNb("+999999999999"), Optional.of("999999999999"));
        Assert.assertEquals(normalizePhoneNb("999999999999"), Optional.of("999999999999"));
        Assert.assertEquals(normalizePhoneNb("999 999 999 999"), Optional.of("999999999999"));
    }

    @Test
    public void shouldNotNormalize() {
        Assert.assertEquals(normalizePhoneNb(null), Optional.empty());
        Assert.assertEquals(normalizePhoneNb(""), Optional.empty());
    }
}