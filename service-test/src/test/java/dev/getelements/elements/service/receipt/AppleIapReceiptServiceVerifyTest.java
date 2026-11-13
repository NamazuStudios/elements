package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceiptPurchase;
import dev.getelements.elements.service.appleiap.UserAppleIapReceiptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Function;

import static dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment.SANDBOX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Regression tests for UserAppleIapReceiptService#verifyAndCreateAppleIapReceiptsIfNeeded. Responses
 * from Apple that omit optional fields (in_app, original_purchase_date) previously caused NPEs,
 * surfacing as generic 500s.
 */
public class AppleIapReceiptServiceVerifyTest {

    /**
     * A verified receipt with no in_app purchases (Apple omits the array entirely) must yield an
     * empty list, not an NPE.
     */
    @Test
    public void testVerifyWithMissingInAppListReturnsEmptyList() {

        final var grandUnifiedReceipt = new AppleIapGrandUnifiedReceipt();
        // in_app deliberately left null.

        final var service = newService(grandUnifiedReceipt);

        final var result = service.verifyAndCreateAppleIapReceiptsIfNeeded(SANDBOX, "receipt-data");

        assertNotNull(result);
        assertTrue(result.isEmpty());

    }

    /**
     * A verified receipt whose in_app purchase lacks original_purchase_date must still be stored,
     * defaulting the purchase time (mirroring the Steam service), not NPE.
     */
    @Test
    public void testVerifyWithMissingOriginalPurchaseDateDoesNotThrow() {

        final var grandUnifiedReceipt = new AppleIapGrandUnifiedReceipt();
        // original_purchase_date deliberately left null.
        grandUnifiedReceipt.setInApp(List.of(new AppleIapGrandUnifiedReceiptPurchase()));

        final var service = newService(grandUnifiedReceipt);

        final var result = service.verifyAndCreateAppleIapReceiptsIfNeeded(SANDBOX, "receipt-data");

        assertNotNull(result);
        assertEquals(result.size(), 1);

    }

    private UserAppleIapReceiptService newService(final AppleIapGrandUnifiedReceipt grandUnifiedReceipt) {

        final var invoker = mock(AppleIapVerifyReceiptInvoker.class);
        when(invoker.invoke()).thenReturn(grandUnifiedReceipt);

        final var builder = mock(AppleIapVerifyReceiptInvoker.Builder.class);
        when(builder.withEnvironment(any())).thenReturn(builder);
        when(builder.withReceiptData(any())).thenReturn(builder);
        when(builder.build()).thenReturn(invoker);

        final var user = new User();
        user.setId("user.0");

        final var appleIapReceipt = new AppleIapReceipt();
        appleIapReceipt.setOriginalTransactionId("originalTransactionId.0");
        appleIapReceipt.setProductId("productId.0");
        appleIapReceipt.setBundleId("dev.getelements.test_app");
        appleIapReceipt.setUser(user);

        final var dozerMapperRegistry = mock(MapperRegistry.class);
        when(dozerMapperRegistry.map(any(), eq(AppleIapReceipt.class))).thenReturn(appleIapReceipt);

        final var receiptDao = mock(ReceiptDao.class);
        when(receiptDao.createReceipt(any())).thenAnswer(inv -> inv.getArgument(0));

        final var transaction = mock(Transaction.class);
        when(transaction.getDao(ReceiptDao.class)).thenReturn(receiptDao);
        when(transaction.performAndClose(any())).thenAnswer(inv -> {
            final Function<Transaction, ?> op = inv.getArgument(0);
            return op.apply(transaction);
        });

        final var elementRegistry = mock(ElementRegistry.class);

        final var service = new UserAppleIapReceiptService();
        service.setUser(user);
        service.setAppleIapVerifyReceiptInvokerBuilderProvider(() -> builder);
        service.setDozerMapper(dozerMapperRegistry);
        service.setObjectMapper(new ObjectMapper());
        service.setReceiptDao(receiptDao);
        service.setTransactionProvider(() -> transaction);
        service.setElementRegistry(elementRegistry);

        return service;

    }

}
