package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.sdk.model.meta.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.invoker.FacebookIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapVerifyReceiptResponse;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookPaymentItem;
import dev.getelements.elements.service.meta.facebookiap.UserFacebookIapReceiptService;
import dev.getelements.elements.service.meta.facebookiap.invoker.DefaultFacebookIapReceiptRequestInvoker;
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
 * Tests that {@link UserFacebookIapReceiptService#verifyAndCreateFacebookIapReceiptIfNeeded}
 * returns reward issuances when the provider confirms the purchase, and returns an empty list
 * (without issuing any rewards) when the provider rejects or cannot confirm the purchase.
 */
public class FacebookIapRewardIssuanceTest extends AbstractReceiptServiceTest {

    private static final String TEST_APP_ID = "fb_test_app_id";
    private static final String TEST_APP_SECRET = "fb_test_app_secret";
    private static final String TEST_APPLICATION_ID = "testApplicationId";

    @Inject
    private UserFacebookIapReceiptService facebookIapReceiptService;

    @Inject
    private ProductBundleService productBundleService;

    @Inject
    private FacebookIapReceiptRequestInvoker facebookIapReceiptRequestInvoker;

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

        // Set up a profile with an application so verifyAndCreate... can proceed
        final var application = new Application();
        application.setId(TEST_APPLICATION_ID);
        final var profile = new Profile();
        profile.setApplication(application);
        when(profileSupplier.get()).thenReturn(profile);

        // Set up the Facebook application configuration
        final var fbConfig = new FacebookApplicationConfiguration();
        fbConfig.setApplicationId(TEST_APP_ID);
        fbConfig.setApplicationSecret(TEST_APP_SECRET);
        when(applicationConfigurationDao.getDefaultApplicationConfigurationForApplication(
                eq(TEST_APPLICATION_ID), eq(FacebookApplicationConfiguration.class)))
                .thenReturn(fbConfig);

        // Configure the ProductBundleService to return a test issuance for the test product
        when(productBundleService.processVerifiedPurchase(
                eq(FacebookIapReceiptService.FACEBOOK_IAP_SCHEME),
                eq("redeemable.sku"),
                anyString()))
                .thenReturn(List.of(TEST_ISSUANCE));
    }

    @BeforeMethod
    public void resetInvocations() {
        clearInvocations(productBundleService, facebookIapReceiptRequestInvoker);
    }

    private FacebookIapReceipt buildReceipt(final String purchaseId, final String sku) {
        final var receipt = new FacebookIapReceipt();
        receipt.setPurchaseId(purchaseId);
        receipt.setSku(sku);
        receipt.setUserId("fb_user_001");
        receipt.setGrantTime(System.currentTimeMillis());
        receipt.setExpirationTime(System.currentTimeMillis() + 86400_000L);
        receipt.setReportingId("reporting_001");
        receipt.setDeveloperPayload("payload_001");
        return receipt;
    }

    @Test
    public void testSuccessfulVerificationReturnsRewards() {

        // Mock the Facebook API returning a successful response with items
        when(facebookIapReceiptRequestInvoker.invokeVerify(any(), eq(TEST_APP_ID), eq(TEST_APP_SECRET)))
                .then(invocation -> {
                    final var response = new FacebookIapVerifyReceiptResponse();
                    response.setStatus("completed");
                    final var item = new FacebookPaymentItem();
                    item.setProduct("redeemable.sku");
                    item.setQuantity(1);
                    response.setItems(List.of(item));
                    return response;
                });

        final var receipt = buildReceipt("purchase_fb_001", "redeemable.sku");

        final var result = facebookIapReceiptService.verifyAndCreateFacebookIapReceiptIfNeeded(receipt);

        assertFalse(result.isEmpty());
        assertEquals(result.getFirst(), TEST_ISSUANCE);
        verify(productBundleService).processVerifiedPurchase(
                FacebookIapReceiptService.FACEBOOK_IAP_SCHEME, "redeemable.sku", "purchase_fb_001");
    }

    @Test
    public void testNullProviderResponseDoesNotIssueRewards() {

        // Provider is unreachable or returns null
        when(facebookIapReceiptRequestInvoker.invokeVerify(any(), any(), any())).thenReturn(null);

        final var receipt = buildReceipt("purchase_fb_002", "redeemable.sku");

        final var result = facebookIapReceiptService.verifyAndCreateFacebookIapReceiptIfNeeded(receipt);

        assertTrue(result.isEmpty());
        verify(productBundleService, never()).processVerifiedPurchase(any(), any(), any());
    }

    @Test
    public void testProviderResponseWithNullItemsDoesNotIssueRewards() {

        // Provider responds but returns no items (purchase rejected or pending)
        when(facebookIapReceiptRequestInvoker.invokeVerify(any(), any(), any()))
                .then(invocation -> {
                    final var response = new FacebookIapVerifyReceiptResponse();
                    response.setStatus("failed");
                    response.setItems(null);
                    return response;
                });

        final var receipt = buildReceipt("purchase_fb_003", "redeemable.sku");

        final var result = facebookIapReceiptService.verifyAndCreateFacebookIapReceiptIfNeeded(receipt);

        assertTrue(result.isEmpty());
        verify(productBundleService, never()).processVerifiedPurchase(any(), any(), any());
    }

    public static class TestModule extends AbstractReceiptServiceTest.AbstractTestModule {

        @Override
        protected void configure() {

            bind(FacebookIapReceiptRequestInvoker.class)
                    .toInstance(mock(DefaultFacebookIapReceiptRequestInvoker.class));

            bind(Client.class).toInstance(mock(Client.class));

            super.configure();
        }

    }

}
