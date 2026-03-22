package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.steam.SteamIapReceipt;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import dev.getelements.elements.sdk.service.steam.SteamIapReceiptService;
import dev.getelements.elements.sdk.service.steam.client.invoker.SteamIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;
import dev.getelements.elements.service.steam.UserSteamIapReceiptService;
import dev.getelements.elements.service.steam.invoker.DefaultSteamIapReceiptRequestInvoker;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Supplier;

import static com.google.inject.Guice.createInjector;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Tests Steam IAP reward issuance behavior:
 * <ul>
 *   <li>{@link UserSteamIapReceiptService#verifyAndCreateSteamIapReceiptIfNeeded}: must create a
 *       receipt and issue rewards when Steam confirms a Committed transaction, and must reject
 *       non-Committed statuses.</li>
 *   <li>{@link UserSteamIapReceiptService#getOrCreateRewardIssuances}: must return reward
 *       issuances from the configured product bundle for the item ID.</li>
 * </ul>
 */
public class SteamIapRewardIssuanceTest extends AbstractReceiptServiceTest {

    private static final String TEST_PUBLISHER_KEY = "steam_test_publisher_key";
    private static final String TEST_APP_ID = "480";
    private static final String TEST_APPLICATION_ID = "testSteamApplicationId";
    private static final String TEST_STEAM_ID = "76561197960265728";
    private static final String TEST_ORDER_ID = "steam_order_001";
    private static final String REDEEMABLE_ITEM_ID = "redeemable.item";

    @Inject
    private UserSteamIapReceiptService steamIapReceiptService;

    @Inject
    private ProductBundleService productBundleService;

    @Inject
    private SteamIapReceiptRequestInvoker steamIapReceiptRequestInvoker;

    @Inject
    private ApplicationConfigurationDao applicationConfigurationDao;

    @Inject
    private Supplier<Profile> profileSupplier;

    private static final RewardIssuance TEST_ISSUANCE = new RewardIssuance();

    @BeforeClass
    @Override
    public void setup() {
        final var injector = createInjector(new TestModule());
        injector.injectMembers(this);
        super.setup();

        final var application = new Application();
        application.setId(TEST_APPLICATION_ID);

        final var profile = new Profile();
        profile.setApplication(application);
        when(profileSupplier.get()).thenReturn(profile);

        final var steamConfig = new SteamApplicationConfiguration();
        steamConfig.setPublisherKey(TEST_PUBLISHER_KEY);
        steamConfig.setAppId(TEST_APP_ID);

        when(applicationConfigurationDao.getDefaultApplicationConfigurationForApplication(
                eq(TEST_APPLICATION_ID), eq(SteamApplicationConfiguration.class)))
                .thenReturn(steamConfig);

        when(productBundleService.processVerifiedPurchase(
                eq(SteamIapReceiptService.STEAM_IAP_SCHEME),
                eq(REDEEMABLE_ITEM_ID),
                anyString()))
                .thenReturn(List.of(TEST_ISSUANCE));
    }

    @BeforeMethod
    public void resetInvocations() {
        clearInvocations(productBundleService, steamIapReceiptRequestInvoker);
    }

    @Test
    public void testCommittedTransactionCreatesReceiptAndIssuance() {

        when(steamIapReceiptRequestInvoker.invokeQueryTxn(
                eq(TEST_PUBLISHER_KEY), eq(TEST_APP_ID), eq(TEST_ORDER_ID)))
                .thenReturn(buildQueryTxnResponse("Committed", REDEEMABLE_ITEM_ID));

        final var receipt = steamIapReceiptService.verifyAndCreateSteamIapReceiptIfNeeded(TEST_ORDER_ID);

        assertNotNull(receipt);
        assertEquals(receipt.getOrderId(), TEST_ORDER_ID);
        assertEquals(receipt.getItemId(), REDEEMABLE_ITEM_ID);
        assertEquals(receipt.getStatus(), "Committed");

        final var issuances = steamIapReceiptService.getOrCreateRewardIssuances(receipt);
        assertFalse(issuances.isEmpty());
        assertEquals(issuances.getFirst(), TEST_ISSUANCE);
    }

    @Test
    public void testApprovedTransactionCreatesReceipt() {

        when(steamIapReceiptRequestInvoker.invokeQueryTxn(
                eq(TEST_PUBLISHER_KEY), eq(TEST_APP_ID), eq("steam_order_approved")))
                .thenReturn(buildQueryTxnResponse("Approved", REDEEMABLE_ITEM_ID));

        final var receipt = steamIapReceiptService.verifyAndCreateSteamIapReceiptIfNeeded("steam_order_approved");

        assertNotNull(receipt);
        assertEquals(receipt.getStatus(), "Approved");
    }

    @Test(expectedExceptions = Exception.class)
    public void testRefundedTransactionIsRejected() {

        when(steamIapReceiptRequestInvoker.invokeQueryTxn(
                eq(TEST_PUBLISHER_KEY), eq(TEST_APP_ID), eq("steam_order_refunded")))
                .thenReturn(buildQueryTxnResponse("Refunded", REDEEMABLE_ITEM_ID));

        steamIapReceiptService.verifyAndCreateSteamIapReceiptIfNeeded("steam_order_refunded");
    }

    @Test
    public void testGetOrCreateRewardIssuancesReturnsRewards() {

        final var receipt = new SteamIapReceipt();
        receipt.setOrderId("steam_order_rewards");
        receipt.setItemId(REDEEMABLE_ITEM_ID);

        final var result = steamIapReceiptService.getOrCreateRewardIssuances(receipt);

        assertFalse(result.isEmpty());
        assertEquals(result.getFirst(), TEST_ISSUANCE);
        verify(productBundleService).processVerifiedPurchase(
                SteamIapReceiptService.STEAM_IAP_SCHEME, REDEEMABLE_ITEM_ID, "steam_order_rewards");
    }

    @Test
    public void testGetOrCreateRewardIssuancesNoBundleConfiguredReturnsEmpty() {

        final var receipt = new SteamIapReceipt();
        receipt.setOrderId("steam_order_no_bundle");
        receipt.setItemId("unconfigured.item");

        final var result = steamIapReceiptService.getOrCreateRewardIssuances(receipt);

        assertTrue(result.isEmpty());
    }

    private SteamIapQueryTxnResponse buildQueryTxnResponse(final String status, final String itemId) {
        final var lineItem = new SteamIapQueryTxnResponse.LineItem();
        lineItem.setItemId(itemId);
        lineItem.setQuantity("1");
        lineItem.setAmount("499");
        lineItem.setItemStatus("Succeeded");

        final var lineItems = new SteamIapQueryTxnResponse.LineItems();
        lineItems.setLineItemList(List.of(lineItem));

        final var params = new SteamIapQueryTxnResponse.Params();
        params.setOrderId(TEST_ORDER_ID);
        params.setTransactionId("txn_steam_001");
        params.setSteamId(TEST_STEAM_ID);
        params.setStatus(status);
        params.setCurrency("USD");
        params.setTime("2024-01-01T00:00:00Z");
        params.setCountry("US");
        params.setPrice("499");
        params.setVat("0");

        final var response = new SteamIapQueryTxnResponse.Response();
        response.setResult("OK");
        response.setParams(params);
        response.setLineItems(lineItems);

        final var txnResponse = new SteamIapQueryTxnResponse();
        txnResponse.setResponse(response);
        return txnResponse;
    }

    public static class TestModule extends AbstractTestModule {

        @Override
        protected void configure() {
            bind(SteamIapReceiptRequestInvoker.class)
                    .toInstance(mock(DefaultSteamIapReceiptRequestInvoker.class));
            bind(Client.class).toInstance(mock(Client.class));
            super.configure();
        }

    }

}
