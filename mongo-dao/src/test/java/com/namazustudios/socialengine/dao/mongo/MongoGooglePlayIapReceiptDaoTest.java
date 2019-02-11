package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.googleplayiapreceipt.GooglePlayIapReceipt;
import static com.namazustudios.socialengine.model.googleplayiapreceipt.GooglePlayIapReceipt.PURCHASE_STATE_PURCHASED;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.util.Date;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static java.lang.System.currentTimeMillis;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoGooglePlayIapReceiptDaoTest {
    private static final int INVOCATION_COUNT = 10;

    private UserDao userDao;

    private GooglePlayIapReceiptDao googlePlayIapReceiptDao;

    private User testUser;

    @BeforeClass
    public void createTestUser() {
        testUser = new User();
        testUser.setName("testy.mctesterson.5");
        testUser.setEmail("testy.mctesterson.5@example.com");
        testUser.setLevel(USER);

        testUser = getUserDao().createOrReactivateUser(testUser);
        testUser = getUserDao().createOrReactivateUser(testUser);
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateGooglePlayIapReceipt(ITestContext testContext) {
        final int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();

        final GooglePlayIapReceipt googlePlayIapReceipt = new GooglePlayIapReceipt();
        googlePlayIapReceipt.setOrderId("orderId." + invocation);
        googlePlayIapReceipt.setUser(testUser);
        googlePlayIapReceipt.setPurchaseToken("purchaseToken." + invocation);
        googlePlayIapReceipt.setProductId("productId." + invocation);
        googlePlayIapReceipt.setConsumptionState(0);
        googlePlayIapReceipt.setDeveloperPayload("custom_payload" + invocation);
        googlePlayIapReceipt.setKind("androidpublisher#productPurchase");
        googlePlayIapReceipt.setPurchaseState(PURCHASE_STATE_PURCHASED);
        googlePlayIapReceipt.setPurchaseTimeMillis(currentTimeMillis());

        final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                getGooglePlayIapReceiptDao().getOrCreateGooglePlayIapReceipt(googlePlayIapReceipt);

        assertNotNull(resultGooglePlayIapReceipt);
        // the GooglePlayIapReceipt is keyed by the orderId and no other params are dynamically generated, so
        // we can just check for POJO equality
        assertEquals(googlePlayIapReceipt, resultGooglePlayIapReceipt);
    }

    @DataProvider
    public Object[][] getGooglePlayIapReceipts() {
        final Object[][] objects = getGooglePlayIapReceiptDao()
                .getGooglePlayIapReceipts(testUser, 0, 20)
                .getObjects()
                .stream()
                .map(ri -> new Object[]{ri})
                .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @Test(dataProvider = "getGooglePlayIapReceipts", dependsOnMethods = "testCreateGooglePlayIapReceipt")
    public void testGetGooglePlayIapReceiptsFromGetOrCreateGooglePlayIapReceipt(
            final GooglePlayIapReceipt googlePlayIapReceipt
    ) {
        final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                getGooglePlayIapReceiptDao().getOrCreateGooglePlayIapReceipt(googlePlayIapReceipt);

        assertNotNull(resultGooglePlayIapReceipt);
        assertEquals(resultGooglePlayIapReceipt, googlePlayIapReceipt);
    }

    @Test(dataProvider = "getGooglePlayIapReceipts", dependsOnMethods = "testCreateGooglePlayIapReceipt")
    public void testGetGooglePlayIapReceiptByOrderId(final GooglePlayIapReceipt googlePlayIapReceipt) {
        final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                getGooglePlayIapReceiptDao().getGooglePlayIapReceipt(googlePlayIapReceipt.getOrderId());

        assertNotNull(resultGooglePlayIapReceipt);
        assertEquals(resultGooglePlayIapReceipt, googlePlayIapReceipt);
    }

    @Test(dataProvider = "getGooglePlayIapReceipts", dependsOnMethods = "testCreateGooglePlayIapReceipt")
    public void testDisallowedUpsertForGetOrCreateGooglePlayIapReceipt(
            final GooglePlayIapReceipt googlePlayIapReceipt) {
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
                getGooglePlayIapReceiptDao().getOrCreateGooglePlayIapReceipt(newGooglePlayIapReceipt);

        assertEquals(resultGooglePlayIapReceipt.getOrderId(), googlePlayIapReceipt.getOrderId());
    }

    @Test(dataProvider = "getGooglePlayIapReceipts",
            dependsOnMethods = {
                    "testGetGooglePlayIapReceiptsFromGetOrCreateGooglePlayIapReceipt",
                    "testGetGooglePlayIapReceiptByOrderId",
                    "testDisallowedUpsertForGetOrCreateGooglePlayIapReceipt"
            })
    public void testDeleteGooglePlayIapReceipt(final GooglePlayIapReceipt googlePlayIapReceipt) {
        getGooglePlayIapReceiptDao().deleteGooglePlayIapReceipt(googlePlayIapReceipt.getOrderId());

        try {
            final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                    getGooglePlayIapReceiptDao().getGooglePlayIapReceipt(googlePlayIapReceipt.getOrderId());
            assertNull(resultGooglePlayIapReceipt);
        }
        catch (NotFoundException e) {
            // this is the expected result
        }
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public GooglePlayIapReceiptDao getGooglePlayIapReceiptDao() {
        return googlePlayIapReceiptDao;
    }

    @Inject
    public void setGooglePlayIapReceiptDao(GooglePlayIapReceiptDao googlePlayIapReceiptDao) {
        this.googlePlayIapReceiptDao = googlePlayIapReceiptDao;
    }
}
