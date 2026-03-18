package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.invoker.OculusIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.service.meta.oculusiap.UserOculusIapReceiptService;
import dev.getelements.elements.service.meta.oculusiap.invoker.DefaultOculusIapReceiptRequestInvoker;
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
 * Tests Oculus IAP reward issuance behavior:
 * <ul>
 *   <li>{@link UserOculusIapReceiptService#consumeAndRecordOculusIapReceipt}: must NOT issue
 *       rewards when the consume response indicates failure.</li>
 *   <li>{@link UserOculusIapReceiptService#getOrCreateRewardIssuances}: must return reward
 *       issuances from the configured product bundle.</li>
 * </ul>
 */
public class OculusIapRewardIssuanceTest extends AbstractReceiptServiceTest {

    private static final String TEST_APP_ID = "oculus_test_app_id";
    private static final String TEST_APP_SECRET = "oculus_test_secret";
    private static final String TEST_APPLICATION_ID = "testOculusApplicationId";

    @Inject
    private UserOculusIapReceiptService oculusIapReceiptService;

    @Inject
    private ProductBundleService productBundleService;

    @Inject
    private OculusIapReceiptRequestInvoker oculusIapReceiptRequestInvoker;

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

        // Set up a profile with an application
        final var application = new Application();
        application.setId(TEST_APPLICATION_ID);

        final var profile = new Profile();
        profile.setApplication(application);
        when(profileSupplier.get()).thenReturn(profile);

        // Set up the Oculus application configuration
        final var oculusConfig = new OculusApplicationConfiguration();
        oculusConfig.setApplicationId(TEST_APP_ID);
        oculusConfig.setApplicationSecret(TEST_APP_SECRET);

        when(applicationConfigurationDao.getDefaultApplicationConfigurationForApplication(
                eq(TEST_APPLICATION_ID), eq(OculusApplicationConfiguration.class)))
                .thenReturn(oculusConfig);

        // Configure rewards for a specific SKU
        when(productBundleService.processVerifiedPurchase(
                eq(OculusIapReceiptService.OCULUS_IAP_SCHEME),
                eq("redeemable.sku"),
                anyString()))
                .thenReturn(List.of(TEST_ISSUANCE));
    }

    @BeforeMethod
    public void resetInvocations() {
        clearInvocations(productBundleService, oculusIapReceiptRequestInvoker);
    }

    private OculusIapReceipt buildReceipt(final String purchaseId, final String sku) {
        final var receipt = new OculusIapReceipt();
        receipt.setPurchaseId(purchaseId);
        receipt.setSku(sku);
        receipt.setUserId("oculus_user_001");
        receipt.setGrantTime(System.currentTimeMillis());
        receipt.setExpirationTime(System.currentTimeMillis() + 86400_000L);
        receipt.setReportingId("reporting_oculus_001");
        receipt.setDeveloperPayload("payload_oculus_001");
        return receipt;
    }

    @Test
    public void testSuccessfulConsumeIssuesRewards() {

        when(oculusIapReceiptRequestInvoker.invokeConsume(any(), eq(TEST_APP_ID), eq(TEST_APP_SECRET)))
                .then(invocation -> {
                    final var response = new OculusIapConsumeResponse();
                    response.setSuccess(true);
                    return response;
                });

        final var receipt = buildReceipt("purchase_oculus_001", "redeemable.sku");

        oculusIapReceiptService.consumeAndRecordOculusIapReceipt(receipt);

        // The consume method calls processVerifiedPurchase when successful
        verify(productBundleService).processVerifiedPurchase(
                OculusIapReceiptService.OCULUS_IAP_SCHEME, "redeemable.sku", "purchase_oculus_001");
    }

    @Test
    public void testFailedConsumeDoesNotIssueRewards() {

        // Provider rejects the consume request
        when(oculusIapReceiptRequestInvoker.invokeConsume(any(), eq(TEST_APP_ID), eq(TEST_APP_SECRET)))
                .then(invocation -> {
                    final var response = new OculusIapConsumeResponse();
                    response.setSuccess(false);
                    return response;
                });

        final var receipt = buildReceipt("purchase_oculus_002", "redeemable.sku");

        oculusIapReceiptService.consumeAndRecordOculusIapReceipt(receipt);

        // No rewards should be issued when consume fails
        verify(productBundleService, never()).processVerifiedPurchase(any(), any(), any());
    }

    @Test
    public void testGetOrCreateRewardIssuancesReturnsRewards() {

        final var receipt = buildReceipt("purchase_oculus_003", "redeemable.sku");
        final var result = oculusIapReceiptService.getOrCreateRewardIssuances(receipt);

        assertFalse(result.isEmpty());
        assertEquals(result.getFirst(), TEST_ISSUANCE);
        verify(productBundleService).processVerifiedPurchase(
                OculusIapReceiptService.OCULUS_IAP_SCHEME, "redeemable.sku", "purchase_oculus_003");
    }

    @Test
    public void testGetOrCreateRewardIssuancesNoBundleConfiguredReturnsEmpty() {

        final var receipt = buildReceipt("purchase_oculus_004", "unconfigured.sku");
        final var result = oculusIapReceiptService.getOrCreateRewardIssuances(receipt);

        assertTrue(result.isEmpty());
    }

    public static class TestModule extends AbstractTestModule {

        @Override
        protected void configure() {

            bind(OculusIapReceiptRequestInvoker.class)
                    .toInstance(mock(DefaultOculusIapReceiptRequestInvoker.class));

            bind(Client.class).toInstance(mock(Client.class));

            super.configure();
        }

    }

}
