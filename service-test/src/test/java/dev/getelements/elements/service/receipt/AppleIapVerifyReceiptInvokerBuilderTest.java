package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.service.appleiap.client.invoker.builder.DefaultAppleIapVerifyReceiptInvokerBuilder;
import org.testng.annotations.Test;

/**
 * Regression tests: missing or blank receiptData is client error data and must surface as a 400-class
 * InvalidDataException. Previously the builder threw IllegalStateException, which mapped to a
 * generic 500.
 */
public class AppleIapVerifyReceiptInvokerBuilderTest {

    @Test
    public void testNullReceiptDataThrowsInvalidDataException() {

        final var builder = new DefaultAppleIapVerifyReceiptInvokerBuilder();

        try {
            builder.withReceiptData(null).build();
        } catch (InvalidDataException e) {
            return; // expected
        }

        throw new AssertionError("Expected InvalidDataException for null receiptData.");

    }

    @Test
    public void testBlankReceiptDataThrowsInvalidDataException() {

        final var builder = new DefaultAppleIapVerifyReceiptInvokerBuilder();

        try {
            builder.withReceiptData("   ").build();
        } catch (InvalidDataException e) {
            return; // expected
        }

        throw new AssertionError("Expected InvalidDataException for blank receiptData.");

    }

}
