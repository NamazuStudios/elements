package dev.getelements.elements.jrpc;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.exception.ErrorCode;
import dev.getelements.elements.rt.ResponseCode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static dev.getelements.elements.rt.jrpc.JsonRpcError.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class TestConstants {

    @DataProvider
    public Object[][] errorCodes() {
        return Stream.of(ErrorCode.values())
                .map(c -> new Object[]{c})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] responseCodes() {
        return Stream.of(ResponseCode.values())
                .map(c -> new Object[]{c})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "errorCodes")
    public void testErrorCode(final ErrorCode errorCode) {
        check(errorCode.ordinal());
    }

    @Test
    public void testPhoneRegex() {
        final List<String> validPhones = asList("1234567890", "12-2343-245", "+1231244234", "(23)123243243", "+234325-453-34","+(23)234-3424-34",
                "123 324 43245", "+123 2312 423 3525", "(+123)423 4234-543");

        final List<String> invalidPhones = asList("1234", "5432545432543532454654", "rrewqtrew", "1234 34545r23", "23@432-234543");

        validPhones.forEach(validPhone -> {
            assertTrue(validPhone.matches(Constants.Regexp.PHONE_NB));
        });


        invalidPhones.forEach(invalidPhone -> {
            assertFalse(invalidPhone.matches(Constants.Regexp.PHONE_NB));
        });
    }

    @Test
    public void testFirstNameRegex() {
        final List<String> validNames = asList("John", "Wieslaw", "Jo", "Sir John", "John2");

        final List<String> invalidNames = asList("F", "T#$TEW#@$", "JohnJohnJohnJohnJohnJohnJohnJohnJohn");

        validNames.forEach(validName -> {
            assertTrue(validName.matches(Constants.Regexp.FIRST_NAME));
        });


        invalidNames.forEach(invalidName -> {
            assertFalse(invalidName.matches(Constants.Regexp.FIRST_NAME));
        });
    }

    @Test
    public void testLastNameRegex() {
        final List<String> validNames = asList("Dear", "Ping Pong", "Dwarfs12", "VerylongLastnameeeeeeee");

        final List<String> invalidNames = asList("F", "T#$TEW#@$", "DearDearDearDearDearDearDearDearDearDearDear");

        validNames.forEach(validName -> {
            assertTrue(validName.matches(Constants.Regexp.LAST_NAME));
        });


        invalidNames.forEach(invalidName -> {
            assertFalse(invalidName.matches(Constants.Regexp.LAST_NAME));
        });
    }

    public void check(final int value) {
        switch (value) {
            case PARSE_ERROR:
            case INVALID_REQUEST:
            case METHOD_NOT_FOUND:
            case INVALID_PARAMETERS:
            case INTERNAL_ERROR:
                fail("Error Code Conflicts with JSON RPC Standard");
                break;
            default:
                break;
        }

        if (value >= SERVER_ERROR_MIN && value <= SERVER_ERROR_MAX) {

            final var msg = format(
                    "Code falls in reserved range of [%d, %d]",
                    SERVER_ERROR_MIN, SERVER_ERROR_MAX
            );

            fail(msg);

        }

    }

    @Test(dataProvider = "responseCodes")
    public void testResponseCode(final ResponseCode responseCode) {
        check(responseCode.getCode());
    }

}
