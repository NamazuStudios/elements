package dev.getelements.elements.service.receipt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.meta.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.service.meta.facebookiap.UserFacebookIapReceiptService;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regression test: a serialization failure while persisting a Facebook IAP receipt must surface as
 * an InternalException (mapped, logged). Previously the service threw java.lang.InternalError,
 * which escapes the REST ExceptionMapper entirely.
 */
public class FacebookIapReceiptServiceErrorTest {

    @Test
    public void testSerializationFailureThrowsInternalExceptionNotInternalError() throws Exception {

        final var objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any()))
                .thenAnswer(invocation -> sneakyThrow(new JsonProcessingException("Simulated serialization failure.") {}));

        final var service = new UserFacebookIapReceiptService();
        service.setObjectMapper(objectMapper);

        try {
            service.getOrCreateFacebookIapReceipt(new FacebookIapReceipt());
        } catch (InternalException e) {
            return; // expected
        }

        throw new AssertionError("Expected InternalException on serialization failure.");

    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T sneakyThrow(final Throwable throwable) throws T {
        throw (T) throwable;
    }

}
