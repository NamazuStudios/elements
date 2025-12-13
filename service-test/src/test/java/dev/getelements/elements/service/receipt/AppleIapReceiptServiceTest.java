package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.service.appleiap.UserAppleIapReceiptService;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Date;

import static com.google.inject.Guice.createInjector;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class AppleIapReceiptServiceTest extends AbstractReceiptServiceTest {

    @Inject
    private UserAppleIapReceiptService appleIapReceiptService;

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
    public void testCreateAppleIapReceipt(ITestContext testContext) {

        final var appleIapReceipt = getAppleIapReceipt(testContext);

        try {
            appleIapReceiptService.getAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());
        } catch (Exception e) {
            // this is the expected result
        }

        final var resultAppleIapReceipt = appleIapReceiptService.getOrCreateAppleIapReceipt(appleIapReceipt);

        assertNotNull(resultAppleIapReceipt);

        assertEquals(resultAppleIapReceipt.getOriginalTransactionId(), appleIapReceipt.getOriginalTransactionId());
        assertEquals(resultAppleIapReceipt.getReceiptData(), appleIapReceipt.getReceiptData());
        assertEquals(resultAppleIapReceipt.getQuantity(), appleIapReceipt.getQuantity());
        assertEquals(resultAppleIapReceipt.getProductId(), appleIapReceipt.getProductId());
        assertEquals(resultAppleIapReceipt.getBundleId(), appleIapReceipt.getBundleId());

    }

    @NotNull
    private AppleIapReceipt getAppleIapReceipt(ITestContext testContext) {
        final int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();
        final var user = new User();
        user.setId(testUser.getId());
        final var appleIapReceipt = new AppleIapReceipt();
        appleIapReceipt.setOriginalTransactionId("id." + invocation);
        appleIapReceipt.setUser(user);
        appleIapReceipt.setReceiptData("receiptData." + invocation);
        appleIapReceipt.setQuantity(invocation + 1);
        appleIapReceipt.setProductId("productId." + invocation);
        appleIapReceipt.setBundleId("dev.getelements.test_app");
        appleIapReceipt.setOriginalPurchaseDate(new Date());
        return appleIapReceipt;
    }

    @DataProvider
    public Object[][] getAppleIapReceipts() {
        final Object[][] objects = appleIapReceiptService
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
                appleIapReceiptService.getOrCreateAppleIapReceipt(appleIapReceipt);

        assertNotNull(resultAppleIapReceipt);
        assertEquals(resultAppleIapReceipt, appleIapReceipt);
    }

    @Test(dataProvider = "getAppleIapReceipts", dependsOnMethods = "testCreateAppleIapReceipt")
    public void testGetAppleIapReceiptByOriginalTransactionId(final AppleIapReceipt appleIapReceipt) {
        final AppleIapReceipt resultAppleIapReceipt =
                appleIapReceiptService.getAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());

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
                appleIapReceiptService.getOrCreateAppleIapReceipt(newAppleIapReceipt);

        assertEquals(resultAppleIapReceipt.getOriginalTransactionId(), appleIapReceipt.getOriginalTransactionId());
    }

    @Test(dataProvider = "getAppleIapReceipts",
            dependsOnMethods = {
                    "testGetAppleIapReceiptsFromGetOrCreateAppleIapReceipt",
                    "testGetAppleIapReceiptByOriginalTransactionId",
                    "testDisallowedUpsertForGetOrCreateAppleIapReceipt"
            })
    public void testDeleteAppleIapReceipt(final AppleIapReceipt appleIapReceipt) {
        appleIapReceiptService.deleteAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());

        try {
            final AppleIapReceipt resultAppleIapReceipt =
                    appleIapReceiptService.getAppleIapReceipt(appleIapReceipt.getOriginalTransactionId());
            assertNull(resultAppleIapReceipt);
        }
        catch (NotFoundException e) {
            // this is the expected result
        }
    }

    public static class TestModule extends AbstractTestModule {
        
        @Override
        protected void configure() {

            super.configure();
            
            bind(AppleIapVerifyReceiptInvoker.Builder.class).toInstance(mock(AppleIapVerifyReceiptInvoker.Builder.class));
            bind(AppleIapReceiptDao.class).toInstance(mock(AppleIapReceiptDao.class));
        }

    }
}
