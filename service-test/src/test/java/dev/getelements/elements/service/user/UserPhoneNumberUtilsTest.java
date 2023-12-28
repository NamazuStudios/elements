package dev.getelements.elements.service.user;

import org.testng.annotations.Test;

import java.util.Optional;

import static dev.getelements.elements.service.user.UserPhoneNumberUtils.normalizePhoneNb;
import static org.testng.Assert.assertEquals;


public class UserPhoneNumberUtilsTest {

    @Test
    public void shouldNormalizeCommonNumbers() {
        final String phone = "+17034567890";
        assertEquals(normalizePhoneNb("+17034567890"), Optional.of("+17034567890"));
        assertEquals(normalizePhoneNb("+13 034 567 890"), Optional.of("+13034567890"));
        assertEquals(normalizePhoneNb("0048782733456"), Optional.of("0048782733456"));
        assertEquals(normalizePhoneNb("0048 782-733-456"), Optional.of("0048782733456"));
    }

    @Test
    public void shouldNormalizeUnknownRegionNumber() {
        assertEquals(normalizePhoneNb("+999999999999"), Optional.of("999999999999"));
        assertEquals(normalizePhoneNb("999999999999"), Optional.of("999999999999"));
        assertEquals(normalizePhoneNb("999 999 999 999"), Optional.of("999999999999"));
    }

    @Test
    public void shouldNotNormalize() {
        assertEquals(normalizePhoneNb(null), Optional.empty());
        assertEquals(normalizePhoneNb(""), Optional.empty());
    }
}