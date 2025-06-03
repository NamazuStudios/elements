package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.AppleIapReceiptDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import org.testng.ITestContext;
import org.testng.annotations.*;

import jakarta.inject.Inject;

import java.util.Date;
import java.util.Objects;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoAppleIapReceiptDaoTest {
    private static final int INVOCATION_COUNT = 10;

    private UserDao userDao;

    private AppleIapReceiptDao appleIapReceiptDao;

    private User testUser;

    private UserTestFactory userTestFactory;

    @BeforeClass
    public void createTestUser() {
        testUser = getUserTestFactory().createTestUser();
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateAppleIapReceipt(ITestContext testContext) {
        final int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();

        final var appleIapReceipt = new AppleIapReceipt();
        appleIapReceipt.setOriginalTransactionId("id." + invocation);
        appleIapReceipt.setUser(testUser);
        appleIapReceipt.setReceiptData("receiptData." + invocation);
        appleIapReceipt.setQuantity(invocation + 1);
        appleIapReceipt.setProductId("productId." + invocation);
        appleIapReceipt.setBundleId("dev.getelements.test_app");
        appleIapReceipt.setOriginalPurchaseDate(new Date());

        try {
            getAppleIapReceiptDao().getAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());
        } catch (NotFoundException e) {
            // this is the expected result
        }

        final var resultAppleIapReceipt = getAppleIapReceiptDao().getOrCreateAppleIapReceipt(appleIapReceipt);

        assertNotNull(resultAppleIapReceipt);

        assertEquals(resultAppleIapReceipt.getOriginalTransactionId(), appleIapReceipt.getOriginalTransactionId());
        assertEquals(resultAppleIapReceipt.getUser(), appleIapReceipt.getUser());
        assertEquals(resultAppleIapReceipt.getReceiptData(), appleIapReceipt.getReceiptData());
        assertEquals(resultAppleIapReceipt.getQuantity(), appleIapReceipt.getQuantity());
        assertEquals(resultAppleIapReceipt.getProductId(), appleIapReceipt.getProductId());
        assertEquals(resultAppleIapReceipt.getBundleId(), appleIapReceipt.getBundleId());

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
        newAppleIapReceipt.setBundleId("dev.getelements.test_app");
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

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

}
