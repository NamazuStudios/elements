package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import org.testng.ITestContext;
import org.testng.annotations.*;

import javax.inject.Inject;

import java.util.Date;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoAppleIapReceiptDaoTest {
    private static final int INVOCATION_COUNT = 10;

    private UserDao userDao;

    private AppleIapReceiptDao appleIapReceiptDao;

    private User testUser;

    @BeforeClass
    public void createTestUser() {
        testUser = new User();
        testUser.setName("testy.mctesterson.5");
        testUser.setEmail("testy.mctesterson.5@example.com");
        testUser.setLevel(USER);
        testUser = getUserDao().createOrReactivateUser(testUser);
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateAppleIapReceipt(ITestContext testContext) {
        final int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();

        final AppleIapReceipt appleIapReceipt = new AppleIapReceipt();
        appleIapReceipt.setOriginalTransactionId("id." + invocation);
        appleIapReceipt.setUser(testUser);
        appleIapReceipt.setReceiptData("receiptData." + invocation);
        appleIapReceipt.setQuantity(invocation + 1);
        appleIapReceipt.setProductId("productId." + invocation);
        appleIapReceipt.setBundleId("com.namazustudios.test_app");
        appleIapReceipt.setOriginalPurchaseDate(new Date());

        final AppleIapReceipt resultAppleIapReceipt =
                getAppleIapReceiptDao().getOrCreateAppleIapReceipt(appleIapReceipt);

        assertNotNull(resultAppleIapReceipt);
        // the AppleIapReceipt is keyed by the originalTransactionId and no other params are dynamically generated, so
        // we can just check for POJO equality
        assertEquals(appleIapReceipt, resultAppleIapReceipt);
    }

    @DataProvider
    public Object[][] getAppleIapReceipts() {
        final Object[][] objects = getAppleIapReceiptDao()
                .getAppleIapReceipts(testUser, 0, 20)
                .getObjects()
                .stream()
                .map(ri -> new Object[]{ri})
                .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @Test(dataProvider = "getAppleIapReceipts", dependsOnMethods = "testCreateAppleIapReceipt")
    public void testGetAppleIapReceiptsFromGetOrCreateAppleIapReceipt(final AppleIapReceipt appleIapReceipt) {
        final AppleIapReceipt resultAppleIapReceipt =
                getAppleIapReceiptDao().getOrCreateAppleIapReceipt(appleIapReceipt);

        assertNotNull(resultAppleIapReceipt);
        assertEquals(resultAppleIapReceipt, appleIapReceipt);
    }

    @Test(dataProvider = "getAppleIapReceipts", dependsOnMethods = "testCreateAppleIapReceipt")
    public void testGetAppleIapReceiptByOriginalTransactionId(final AppleIapReceipt appleIapReceipt) {
        final AppleIapReceipt resultAppleIapReceipt =
                getAppleIapReceiptDao().getAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());

        assertNotNull(resultAppleIapReceipt);
        assertEquals(resultAppleIapReceipt, appleIapReceipt);
    }

    @Test(dataProvider = "getAppleIapReceipts", dependsOnMethods = "testCreateAppleIapReceipt")
    public void testDisallowedUpsertForGetOrCreateAppleIapReceipt(final AppleIapReceipt appleIapReceipt) {
        final AppleIapReceipt newAppleIapReceipt = new AppleIapReceipt();
        // attempt to overwrite according to the original transaction id key
        newAppleIapReceipt.setOriginalTransactionId(appleIapReceipt.getOriginalTransactionId());
        newAppleIapReceipt.setUser(testUser);
        newAppleIapReceipt.setReceiptData("receiptData." + -1);
        newAppleIapReceipt.setQuantity(1);
        newAppleIapReceipt.setProductId("productId." + -1);
        newAppleIapReceipt.setBundleId("com.namazustudios.test_app");
        newAppleIapReceipt.setOriginalPurchaseDate(new Date());

        final AppleIapReceipt resultAppleIapReceipt =
                getAppleIapReceiptDao().getOrCreateAppleIapReceipt(newAppleIapReceipt);

        assertEquals(resultAppleIapReceipt.getOriginalTransactionId(), appleIapReceipt.getOriginalTransactionId());
    }

    @Test(dataProvider = "getAppleIapReceipts",
            dependsOnMethods = {
                    "testGetAppleIapReceiptsFromGetOrCreateAppleIapReceipt",
                    "testGetAppleIapReceiptByOriginalTransactionId",
                    "testDisallowedUpsertForGetOrCreateAppleIapReceipt"
            })
    public void testDeleteAppleIapReceipt(final AppleIapReceipt appleIapReceipt) {
        getAppleIapReceiptDao().deleteAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());

        try {
            final AppleIapReceipt resultAppleIapReceipt =
                    getAppleIapReceiptDao().getAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());
            assertNull(resultAppleIapReceipt);
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

    public AppleIapReceiptDao getAppleIapReceiptDao() {
        return appleIapReceiptDao;
    }

    @Inject
    public void setAppleIapReceiptDao(AppleIapReceiptDao appleIapReceiptDao) {
        this.appleIapReceiptDao = appleIapReceiptDao;
    }

}
