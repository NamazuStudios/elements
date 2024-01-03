package dev.getelements.elements.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.util.Objects.isNull;

public class PhoneNormalizer {

    private static final Logger logger = LoggerFactory.getLogger(PhoneNormalizer.class);

    public static Optional<String> normalizePhoneNb(String phoneNb) {
        if(isNull(phoneNb) || phoneNb.isEmpty()) {
            return Optional.empty();
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        final String region = getRegionForPhoneNumber(phoneUtil, phoneNb);
        return (isNull(region) || region.isEmpty()) ?
                normalizeWithoutKnowingRegion(phoneNb) :
                normalizeForRegion(phoneUtil, phoneNb, region);
    }

    private static Optional<String> normalizeWithoutKnowingRegion(String phoneNb) {
        return Optional.of(PhoneNumberUtil.normalizeDiallableCharsOnly(phoneNb).replaceAll("[^0-9]", ""));
    }

    private static Optional<String> normalizeForRegion(PhoneNumberUtil phoneUtil, String phoneNb, String region) {
        try {
            Phonenumber.PhoneNumber parsedNb = phoneUtil.parse(phoneNb, region);
            if (phoneUtil.isValidNumber(parsedNb)
                    && phoneUtil.isPossibleNumberForType(parsedNb, PhoneNumberUtil.PhoneNumberType.MOBILE)) {
                return Optional.of(phoneUtil.format(parsedNb, PhoneNumberUtil.PhoneNumberFormat.E164));
            }
        } catch (NumberParseException ex) {
            logger.warn("Cannot normalize phone nb " + phoneNb);
        }
        return Optional.empty();
    }

    private static String getRegionForPhoneNumber(PhoneNumberUtil phoneUtil, String phoneNumber) {
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, "");
            return phoneUtil.getRegionCodeForNumber(numberProto);
        } catch (NumberParseException e) {
            return null;
        }
    }
}
