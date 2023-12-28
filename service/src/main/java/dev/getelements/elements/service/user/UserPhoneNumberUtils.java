package dev.getelements.elements.service.user;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import dev.getelements.elements.service.profile.SuperUserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.google.i18n.phonenumbers.PhoneNumberUtil.normalizeDiallableCharsOnly;
import static io.netty.util.internal.StringUtil.isNullOrEmpty;

public class UserPhoneNumberUtils {

    private static final Logger logger = LoggerFactory.getLogger(SuperUserProfileService.class);

    public static Optional<String> normalizePhoneNb(String phoneNb) {
        if(isNullOrEmpty(phoneNb)) {
            return Optional.empty();
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        final String region = getRegionForPhoneNumber(phoneUtil, phoneNb);
        return isNullOrEmpty(region) ?
                normalizeWithoutKnowingRegion(phoneUtil, phoneNb) :
                normalizeForRegion(phoneUtil, phoneNb, region);
    }

    private static Optional<String> normalizeWithoutKnowingRegion(PhoneNumberUtil phoneUtil, String phoneNb) {
        return Optional.of(normalizeDiallableCharsOnly(phoneNb).replaceAll("[^0-9]", ""));
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
