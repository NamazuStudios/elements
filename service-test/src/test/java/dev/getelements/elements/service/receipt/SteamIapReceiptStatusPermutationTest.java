package dev.getelements.elements.service.receipt;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import dev.getelements.elements.sdk.service.steam.client.invoker.SteamIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;
import dev.getelements.elements.service.steam.UserSteamIapReceiptService;
import dev.getelements.elements.sdk.ElementRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;

/**
 * Status-permutation coverage for the Steam verification gate (#222): only Steam's final successful
 * state ("Succeeded") qualifies for reward issuance. Every other status -- including statuses that
 * predate the Steam documentation correction ("Committed") and reversible states (Refunded,
 * PartialRefund, Chargedback, fraud-suspected refunds) -- must be rejected, as must transactions
 * with no line items.
 */
public class SteamIapReceiptStatusPermutationTest {

    private UserSteamIapReceiptService steamIapReceiptService;

    private SteamIapReceiptRequestInvoker requestInvoker;

    @BeforeMethod
    public void setUp() {

        requestInvoker = mock(SteamIapReceiptRequestInvoker.class);

        final var application = new Application();
        application.setId("app-1");

        final var profile = new Profile();
        profile.setId("profile-1");
        profile.setApplication(application);
        profile.setUser(new User());

        final var applicationConfiguration = new SteamApplicationConfiguration();
        applicationConfiguration.setAppId("3855060");
        applicationConfiguration.setPublisherKey("publisher-key");
        applicationConfiguration.setSandbox(true);

        final var applicationConfigurationDao =
                mock(dev.getelements.elements.sdk.dao.ApplicationConfigurationDao.class);
        when(applicationConfigurationDao.getDefaultApplicationConfigurationForApplication(
                anyString(), eq(SteamApplicationConfiguration.class)))
                .thenReturn(applicationConfiguration);

        final var receiptDao = mock(ReceiptDao.class);
        when(receiptDao.createReceipt(any())).thenAnswer(inv -> inv.getArgument(0));

        final var transaction = mock(Transaction.class);
        when(transaction.getDao(ReceiptDao.class)).thenReturn(receiptDao);
        when(transaction.performAndClose(any())).thenAnswer(inv -> {
            final var op = (Function<Transaction, ?>) inv.getArgument(0);
            return op.apply(transaction);
        });

        steamIapReceiptService = new UserSteamIapReceiptService();
        steamIapReceiptService.setUser(new User());
        steamIapReceiptService.setCurrentProfileSupplier(() -> profile);
        steamIapReceiptService.setApplicationConfigurationDao(applicationConfigurationDao);
        steamIapReceiptService.setRequestInvoker(requestInvoker);
        steamIapReceiptService.setObjectMapper(new ObjectMapper());
        steamIapReceiptService.setReceiptDao(receiptDao);
        steamIapReceiptService.setTransactionProvider(() -> transaction);
        steamIapReceiptService.setElementRegistry(mock(ElementRegistry.class));
        steamIapReceiptService.setProductBundleService(mock(ProductBundleService.class));

    }

    private SteamIapQueryTxnResponse responseWithStatus(final String status) {

        final var item = new SteamIapQueryTxnResponse.Item();
        item.setItemId("1001");
        item.setQuantity("1");
        item.setAmount("199");
        item.setVat("0");
        item.setItemStatus(status);

        final var params = new SteamIapQueryTxnResponse.Params();
        params.setOrderId("order-1");
        params.setTransactionId("txn-1");
        params.setSteamId("76561198640629018");
        params.setStatus(status);
        params.setCurrency("USD");
        params.setTime("2026-11-13T22:00:55Z");
        params.setCountry("US");
        params.setItems(List.of(item));

        final var response = new SteamIapQueryTxnResponse.Response();
        response.setResult("OK");
        response.setParams(params);

        final var queryTxnResponse = new SteamIapQueryTxnResponse();
        queryTxnResponse.setResponse(response);
        return queryTxnResponse;

    }

    @Test
    public void succeededIsAccepted() {

        when(requestInvoker.invokeQueryTxn(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(responseWithStatus("Succeeded"));

        final var result = steamIapReceiptService.verifyAndCreateSteamIapReceiptIfNeeded("order-1");
        assertEquals(result.getStatus(), "Succeeded");

    }

    @Test
    public void initIsRejected() {
        assertRejected(responseWithStatus("Init"));
    }

    @Test
    public void approvedIsRejected() {
        assertRejected(responseWithStatus("Approved"));
    }

    @Test
    public void failedIsRejected() {
        assertRejected(responseWithStatus("Failed"));
    }

    @Test
    public void refundedIsRejected() {
        assertRejected(responseWithStatus("Refunded"));
    }

    @Test
    public void partialRefundIsRejected() {
        assertRejected(responseWithStatus("PartialRefund"));
    }

    @Test
    public void chargedbackIsRejected() {
        assertRejected(responseWithStatus("Chargedback"));
    }

    @Test
    public void refundedSuspectedFraudIsRejected() {
        assertRejected(responseWithStatus("RefundedSuspectedFraud"));
    }

    @Test
    public void refundedFriendlyFraudIsRejected() {
        assertRejected(responseWithStatus("RefundedFriendlyFraud"));
    }

    /**
     * The undocumented "Committed" status the old code accepted does not exist in Steam's API and
     * must not be accepted.
     */
    @Test
    public void legacyCommittedStatusIsRejected() {
        assertRejected(responseWithStatus("Committed"));
    }

    /**
     * QueryTxn v3 returns the line items under response.params.items[] -- a response with no items
     * (the shape the old code expected under response.lineitems[]) must be rejected, not silently
     * recorded without a product id.
     */
    @Test
    public void missingItemsIsRejected() {

        final var params = new SteamIapQueryTxnResponse.Params();
        params.setOrderId("order-1");
        params.setTransactionId("txn-1");
        params.setSteamId("76561198640629018");
        params.setStatus("Succeeded");
        params.setCurrency("USD");
        params.setTime("2026-11-13T22:00:55Z");
        params.setCountry("US");
        // items deliberately absent

        final var response = new SteamIapQueryTxnResponse.Response();
        response.setResult("OK");
        response.setParams(params);

        final var queryTxnResponse = new SteamIapQueryTxnResponse();
        queryTxnResponse.setResponse(response);

        when(requestInvoker.invokeQueryTxn(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(queryTxnResponse);

        assertThrows(InvalidDataException.class,
                () -> steamIapReceiptService.verifyAndCreateSteamIapReceiptIfNeeded("order-1"));

    }

    private void assertRejected(final SteamIapQueryTxnResponse response) {

        when(requestInvoker.invokeQueryTxn(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(response);

        assertThrows(InvalidDataException.class,
                () -> steamIapReceiptService.verifyAndCreateSteamIapReceiptIfNeeded("order-1"));

    }

}
