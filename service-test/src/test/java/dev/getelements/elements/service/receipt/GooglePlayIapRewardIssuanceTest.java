package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import dev.getelements.elements.service.googleplayiap.UserGooglePlayIapReceiptService;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.inject.Guice.createInjector;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Tests that {@link UserGooglePlayIapReceiptService#getOrCreateRewardIssuances} correctly
 * returns reward issuances from the configured product bundle, and returns an empty list
 * when no bundle is configured for the product.
 */
public class GooglePlayIapRewardIssuanceTest extends AbstractReceiptServiceTest {

    @Inject
    private UserGooglePlayIapReceiptService googlePlayIapReceiptService;

    @Inject
    private ProductBundleService productBundleService;

    private static final RewardIssuance TEST_ISSUANCE = new RewardIssuance();

    @BeforeClass
    @Override
    public void setup() {
        final var injector = createInjector(new TestModule());
        injector.injectMembers(this);
        super.setup();

        when(productBundleService.processVerifiedPurchase(
                eq(GooglePlayIapReceiptService.GOOGLE_IAP_SCHEME),
                eq("redeemable.product"),
                anyString()))
                .thenReturn(List.of(TEST_ISSUANCE));
    }

    @BeforeMethod
    public void resetInvocations() {
        clearInvocations(productBundleService);
    }

    @Test
    public void testReturnsRewardsForConfiguredProduct() {

        final var receipt = new GooglePlayIapReceipt();
        receipt.setProductId("redeemable.product");
        receipt.setOrderId("order_google_001");

        final var result = googlePlayIapReceiptService.getOrCreateRewardIssuances(receipt);

        assertFalse(result.isEmpty());
        assertEquals(result.getFirst(), TEST_ISSUANCE);
        verify(productBundleService).processVerifiedPurchase(
                GooglePlayIapReceiptService.GOOGLE_IAP_SCHEME, "redeemable.product", "order_google_001");
    }

    @Test
    public void testReturnsEmptyWhenNoBundleConfigured() {

        final var receipt = new GooglePlayIapReceipt();
        receipt.setProductId("unknown.product");
        receipt.setOrderId("order_google_no_bundle");

        final var result = googlePlayIapReceiptService.getOrCreateRewardIssuances(receipt);

        assertTrue(result.isEmpty());
    }

    public static class TestModule extends AbstractTestModule {

        @Override
        protected void configure() {

            bind(Client.class).toInstance(mock(Client.class));

            super.configure();
        }

    }

}
