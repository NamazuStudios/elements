package dev.getelements.elements.service.receipt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.service.meta.oculusiap.UserOculusIapReceiptService;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regression test: a serialization failure while persisting an Oculus IAP receipt must surface as
 * an InternalException (mapped, logged). Previously the service threw java.lang.InternalError,
 * which escapes the REST ExceptionMapper entirely.
 */
public class OculusIapReceiptServiceErrorTest {

    @Test
    public void testSerializationFailureThrowsInternalExceptionNotInternalError() throws Exception {

        final var objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any()))
                .thenAnswer(invocation -> sneakyThrow(new JsonProcessingException("Simulated serialization failure.") {}));

        final var service = new UserOculusIapReceiptService();
        service.setObjectMapper(objectMapper);

        try {
            service.getOrCreateOculusIapReceipt(new OculusIapReceipt());
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
