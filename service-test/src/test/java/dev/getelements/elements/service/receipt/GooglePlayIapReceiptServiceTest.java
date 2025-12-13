package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.GooglePlayIapReceiptDao;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.service.googleplayiap.UserGooglePlayIapReceiptService;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.google.inject.Guice.createInjector;
import static dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt.PURCHASE_STATE_PURCHASED;
import static java.lang.System.currentTimeMillis;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

public class GooglePlayIapReceiptServiceTest extends AbstractReceiptServiceTest {

    @Inject
    private UserGooglePlayIapReceiptService googleIapReceiptService;

    @Inject
    private User testUser;

    @BeforeClass
    @Override
    public void setup() {
        final var injector = createInjector(new TestModule());
        injector.injectMembers(this);

        super.setup();
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateGooglePlayIapReceipt(ITestContext testContext) {

        final var googlePlayIapReceipt = getGooglePlayIapReceipt(testContext);

        try {
            googleIapReceiptService.getGooglePlayIapReceipt(googlePlayIapReceipt.getOrderId());
        } catch (Exception e) {
            // this is the expected result
        }

        final var resultGooglePlayIapReceipt = googleIapReceiptService.getOrCreateGooglePlayIapReceipt(googlePlayIapReceipt);

        assertNotNull(resultGooglePlayIapReceipt);

        assertEquals(googlePlayIapReceipt.getOrderId(), resultGooglePlayIapReceipt.getOrderId());
        assertEquals(googlePlayIapReceipt.getUser(), resultGooglePlayIapReceipt.getUser());
        assertEquals(googlePlayIapReceipt.getPurchaseToken(), resultGooglePlayIapReceipt.getPurchaseToken());
        assertEquals(googlePlayIapReceipt.getProductId(), resultGooglePlayIapReceipt.getProductId());
        assertEquals(googlePlayIapReceipt.getConsumptionState(), resultGooglePlayIapReceipt.getConsumptionState());
        assertEquals(googlePlayIapReceipt.getDeveloperPayload(), resultGooglePlayIapReceipt.getDeveloperPayload());
        assertEquals(googlePlayIapReceipt.getKind(), resultGooglePlayIapReceipt.getKind());
        assertEquals(googlePlayIapReceipt.getPurchaseState(), resultGooglePlayIapReceipt.getPurchaseState());
        assertEquals(googlePlayIapReceipt.getPurchaseType(), resultGooglePlayIapReceipt.getPurchaseType());

    }

    @NotNull
    private GooglePlayIapReceipt getGooglePlayIapReceipt(ITestContext testContext) {
        final int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();
        final var user = new User();
        user.setId(testUser.getId());
        final GooglePlayIapReceipt googlePlayIapReceipt = new GooglePlayIapReceipt();
        googlePlayIapReceipt.setOrderId("orderId." + invocation);
        googlePlayIapReceipt.setUser(user);
        googlePlayIapReceipt.setPurchaseToken("purchaseToken." + invocation);
        googlePlayIapReceipt.setProductId("productId." + invocation);
        googlePlayIapReceipt.setConsumptionState(0);
        googlePlayIapReceipt.setDeveloperPayload("custom_payload" + invocation);
        googlePlayIapReceipt.setKind("androidpublisher#productPurchase");
        googlePlayIapReceipt.setPurchaseState(PURCHASE_STATE_PURCHASED);
        googlePlayIapReceipt.setPurchaseTimeMillis(currentTimeMillis());

        return googlePlayIapReceipt;
    }

    @DataProvider
    public Object[][] getGooglePlayIapReceipts() {
        final Object[][] objects = googleIapReceiptService
                .getGooglePlayIapReceipts(testUser, 0, 20)
                .getObjects()
                .stream()
                .map(ri -> new Object[]{ri})
                .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @Test(dataProvider = "getGooglePlayIapReceipts", dependsOnMethods = "testCreateGooglePlayIapReceipt")
    public void testGetGooglePlayIapReceiptsFromGetOrCreateGooglePlayIapReceipt(final GooglePlayIapReceipt googleIapReceipt) {
        final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                googleIapReceiptService.getOrCreateGooglePlayIapReceipt(googleIapReceipt);

        assertNotNull(resultGooglePlayIapReceipt);
        assertEquals(resultGooglePlayIapReceipt, googleIapReceipt);
    }

    @Test(dataProvider = "getGooglePlayIapReceipts", dependsOnMethods = "testCreateGooglePlayIapReceipt")
    public void testGetGooglePlayIapReceiptByOriginalTransactionId(final GooglePlayIapReceipt googleIapReceipt) {
        final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                googleIapReceiptService.getGooglePlayIapReceipt(googleIapReceipt.getOrderId());

        assertNotNull(resultGooglePlayIapReceipt);
        assertEquals(resultGooglePlayIapReceipt, googleIapReceipt);
    }

    @Test(dataProvider = "getGooglePlayIapReceipts", dependsOnMethods = "testCreateGooglePlayIapReceipt")
    public void testDisallowedUpsertForGetOrCreateGooglePlayIapReceipt(final GooglePlayIapReceipt googlePlayIapReceipt) {
        final GooglePlayIapReceipt newGooglePlayIapReceipt = new GooglePlayIapReceipt();
        // attempt to overwrite according to the original transaction id key
        newGooglePlayIapReceipt.setOrderId(googlePlayIapReceipt.getOrderId());
        newGooglePlayIapReceipt.setUser(testUser);

        newGooglePlayIapReceipt.setPurchaseToken("purchaseToken." + -1);
        newGooglePlayIapReceipt.setProductId("productId." + -1);
        newGooglePlayIapReceipt.setConsumptionState(0);
        newGooglePlayIapReceipt.setDeveloperPayload("custom_payload" + -1);
        newGooglePlayIapReceipt.setKind("androidpublisher#productPurchase");
        newGooglePlayIapReceipt.setPurchaseState(PURCHASE_STATE_PURCHASED);
        newGooglePlayIapReceipt.setPurchaseTimeMillis(currentTimeMillis());

        final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                googleIapReceiptService.getOrCreateGooglePlayIapReceipt(newGooglePlayIapReceipt);

        assertEquals(resultGooglePlayIapReceipt.getOrderId(), googlePlayIapReceipt.getOrderId());
    }

    @Test(dataProvider = "getGooglePlayIapReceipts",
            dependsOnMethods = {
                    "testGetGooglePlayIapReceiptsFromGetOrCreateGooglePlayIapReceipt",
                    "testGetGooglePlayIapReceiptByOriginalTransactionId",
                    "testDisallowedUpsertForGetOrCreateGooglePlayIapReceipt"
            })
    public void testDeleteGooglePlayIapReceipt(final GooglePlayIapReceipt googleIapReceipt) {
        googleIapReceiptService.deleteGooglePlayIapReceipt(googleIapReceipt.getOrderId());

        try {
            final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                    googleIapReceiptService.getGooglePlayIapReceipt(googleIapReceipt.getOrderId());
            assertNull(resultGooglePlayIapReceipt);
        }
        catch (NotFoundException e) {
            // this is the expected result
        }
    }

    public static class TestModule extends AbstractTestModule {

        @Override
        protected void configure() {

            super.configure();

            bind(GooglePlayIapReceiptDao.class).toInstance(mock(GooglePlayIapReceiptDao.class));
        }

    }
}
