package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import dev.getelements.elements.service.appleiap.UserAppleIapReceiptService;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;

import static com.google.inject.Guice.createInjector;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Tests that {@link UserAppleIapReceiptService#getOrCreateRewardIssuances} correctly
 * returns reward issuances from the configured product bundle, and returns an empty list
 * when no bundle is configured for the product.
 */
public class AppleIapRewardIssuanceTest extends AbstractReceiptServiceTest {

    @Inject
    private UserAppleIapReceiptService appleIapReceiptService;

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
                eq(AppleIapReceiptService.APPLE_IAP_SCHEME),
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

        final var receipt = new AppleIapReceipt();
        receipt.setProductId("redeemable.product");
        receipt.setOriginalTransactionId("txn_apple_001");
        receipt.setOriginalPurchaseDate(new Date());

        final var result = appleIapReceiptService.getOrCreateRewardIssuances(List.of(receipt));

        assertFalse(result.isEmpty());
        assertEquals(result.getFirst(), TEST_ISSUANCE);
        verify(productBundleService).processVerifiedPurchase(
                AppleIapReceiptService.APPLE_IAP_SCHEME, "redeemable.product", "txn_apple_001");
    }

    @Test
    public void testAccumulatesRewardsAcrossMultipleReceipts() {

        final var receipt1 = new AppleIapReceipt();
        receipt1.setProductId("redeemable.product");
        receipt1.setOriginalTransactionId("txn_apple_002");
        receipt1.setOriginalPurchaseDate(new Date());

        final var receipt2 = new AppleIapReceipt();
        receipt2.setProductId("redeemable.product");
        receipt2.setOriginalTransactionId("txn_apple_003");
        receipt2.setOriginalPurchaseDate(new Date());

        final var result = appleIapReceiptService.getOrCreateRewardIssuances(List.of(receipt1, receipt2));

        assertEquals(result.size(), 2);
        verify(productBundleService, times(2)).processVerifiedPurchase(
                eq(AppleIapReceiptService.APPLE_IAP_SCHEME), eq("redeemable.product"), anyString());
    }

    @Test
    public void testReturnsEmptyWhenNoBundleConfigured() {

        final var receipt = new AppleIapReceipt();
        receipt.setProductId("unknown.product");
        receipt.setOriginalTransactionId("txn_apple_no_bundle");
        receipt.setOriginalPurchaseDate(new Date());

        final var result = appleIapReceiptService.getOrCreateRewardIssuances(List.of(receipt));

        assertTrue(result.isEmpty());
    }

    @Test
    public void testEmptyReceiptListReturnsEmpty() {

        final var result = appleIapReceiptService.getOrCreateRewardIssuances(List.of());

        assertTrue(result.isEmpty());
        verify(productBundleService, never()).processVerifiedPurchase(any(), any(), any());
    }

    public static class TestModule extends AbstractTestModule {

        @Override
        protected void configure() {

            bind(AppleIapVerifyReceiptInvoker.Builder.class)
                    .toInstance(mock(AppleIapVerifyReceiptInvoker.Builder.class));

            super.configure();
        }

    }

}
