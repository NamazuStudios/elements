package dev.getelements.elements.service.receipt;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.ElementRegistry;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

/**
 * Regression tests for #222: Steam Inventory Service purchases (ISteamInventory::StartPurchase)
 * report status "Succeeded" and return their line items under response.params.items[]. The service
 * previously rejected "Succeeded" (it only accepted "Committed"/"Approved") and read line items
 * from response.lineitems[] (never populated by QueryTxn v3), so every valid purchase failed with
 * HTTP 500.
 */
public class SteamIapReceiptVerifyTest {

    private UserSteamIapReceiptService steamIapReceiptService;

    private SteamIapReceiptRequestInvoker requestInvoker;

    private SteamApplicationConfiguration applicationConfiguration;

    @BeforeMethod
    public void setUp() {

        requestInvoker = mock(SteamIapReceiptRequestInvoker.class);

        final var application = new Application();
        application.setId("app-1");

        final var profile = new Profile();
        profile.setId("profile-1");
        profile.setApplication(application);
        profile.setUser(new User());

        applicationConfiguration = new SteamApplicationConfiguration();
        applicationConfiguration.setAppId("3855060");
        applicationConfiguration.setPublisherKey("publisher-key");
        applicationConfiguration.setSandbox(true);

        final var receiptDao = mock(ReceiptDao.class);
        when(receiptDao.createReceipt(any())).thenAnswer(inv -> inv.getArgument(0));

        final var transaction = mock(Transaction.class);
        when(transaction.getDao(ReceiptDao.class)).thenReturn(receiptDao);
        when(transaction.performAndClose(any())).thenAnswer(inv -> {
            final var op = (Function<Transaction, ?>) inv.getArgument(0);
            return op.apply(transaction);
        });

        final var applicationConfigurationDao = mock(dev.getelements.elements.sdk.dao.ApplicationConfigurationDao.class);
        when(applicationConfigurationDao.getDefaultApplicationConfigurationForApplication(
                anyString(), eq(SteamApplicationConfiguration.class)))
                .thenReturn(applicationConfiguration);

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

    /**
     * Builds a QueryTxn response matching Steam's actual v3 payload for a completed Inventory
     * Service purchase (see #222): result OK, status Succeeded, items under response.params.items[].
     */
    private SteamIapQueryTxnResponse successfulResponse() {

        final var item = new SteamIapQueryTxnResponse.Item();
        item.setItemId("1001");
        item.setQuantity("1");
        item.setAmount("199");
        item.setVat("0");
        item.setItemStatus("Succeeded");

        final var params = new SteamIapQueryTxnResponse.Params();
        params.setOrderId("515259549671139737");
        params.setTransactionId("487112211238831449");
        params.setSteamId("76561198640629018");
        params.setStatus("Succeeded");
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
    public void succeededInventoryPurchaseVerifiesAndRecordsItemIdFromParamsItems() {

        when(requestInvoker.invokeQueryTxn(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(successfulResponse());

        final var result = steamIapReceiptService.verifyAndCreateSteamIapReceiptIfNeeded("515259549671139737");

        assertNotNull(result);
        assertEquals(result.getItemId(), "1001");
        assertEquals(result.getStatus(), "Succeeded");
        assertEquals(result.getOrderId(), "515259549671139737");
        assertEquals(result.getAppId(), "3855060");

        // The sandbox configuration must reach the invoker.
        verify(requestInvoker).invokeQueryTxn("publisher-key", "3855060", "515259549671139737", true);

    }

    @Test
    public void nonSucceededStatusIsRejected() {

        final var response = successfulResponse();
        response.getResponse().getParams().setStatus("Approved");

        when(requestInvoker.invokeQueryTxn(anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(response);

        // "Approved" means authorized but not captured -- Elements never calls FinalizeTxn.
        assertThrows(InvalidDataException.class,
                () -> steamIapReceiptService.verifyAndCreateSteamIapReceiptIfNeeded("515259549671139737"));

    }

}
