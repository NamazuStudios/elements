package com.namazustudios.socialengine.jrpc;

import com.namazustudios.socialengine.exception.ErrorCode;
import com.namazustudios.socialengine.rt.ResponseCode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.jrpc.JsonRpcError.*;
import static java.lang.String.format;
import static org.testng.Assert.fail;

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

    @Test(dataProvider = "responseCodes")
    public void testResponseCode(final ResponseCode responseCode) {
        check(responseCode.getCode());
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

}
