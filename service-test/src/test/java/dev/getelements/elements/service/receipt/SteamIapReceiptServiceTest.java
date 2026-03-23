package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.steam.SteamIapReceipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.steam.client.invoker.SteamIapReceiptRequestInvoker;
import dev.getelements.elements.service.steam.UserSteamIapReceiptService;
import dev.getelements.elements.service.steam.invoker.DefaultSteamIapReceiptRequestInvoker;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.google.inject.Guice.createInjector;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

public class SteamIapReceiptServiceTest extends AbstractReceiptServiceTest {

    @Inject
    private UserSteamIapReceiptService steamIapReceiptService;

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
    public void testCreateSteamIapReceipt(ITestContext testContext) {

        final var receipt = buildReceipt(testContext);

        try {
            steamIapReceiptService.getSteamIapReceipt(receipt.getOrderId());
        } catch (Exception e) {
            // expected — receipt does not exist yet
        }

        final var result = steamIapReceiptService.getOrCreateSteamIapReceipt(receipt);

        assertNotNull(result);
        assertEquals(receipt.getOrderId(), result.getOrderId());
        assertEquals(receipt.getTransactionId(), result.getTransactionId());
        assertEquals(receipt.getSteamId(), result.getSteamId());
        assertEquals(receipt.getAppId(), result.getAppId());
        assertEquals(receipt.getItemId(), result.getItemId());
        assertEquals(receipt.getStatus(), result.getStatus());
        assertEquals(receipt.getCurrency(), result.getCurrency());
    }

    private SteamIapReceipt buildReceipt(ITestContext testContext) {
        final int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();
        final var user = new User();
        user.setId(testUser.getId());

        final var receipt = new SteamIapReceipt();
        receipt.setOrderId("orderId.steam." + invocation);
        receipt.setTransactionId("transId.steam." + invocation);
        receipt.setSteamId("76561197960265728");
        receipt.setAppId("480");
        receipt.setItemId("item." + invocation);
        receipt.setStatus("Committed");
        receipt.setCurrency("USD");
        receipt.setUser(user);
        return receipt;
    }

    @DataProvider
    public Object[][] getSteamIapReceipts() {
        final Object[][] objects = steamIapReceiptService
                .getSteamIapReceipts(testUser, 0, 20)
                .getObjects()
                .stream()
                .map(r -> new Object[]{r})
                .toArray(Object[][]::new);
        assertTrue(objects.length > 0);
        return objects;
    }

    @Test(dataProvider = "getSteamIapReceipts", dependsOnMethods = "testCreateSteamIapReceipt")
    public void testGetSteamIapReceiptsFromGetOrCreate(final SteamIapReceipt receipt) {
        final var result = steamIapReceiptService.getOrCreateSteamIapReceipt(receipt);
        assertNotNull(result);
        assertEquals(result, receipt);
    }

    @Test(dataProvider = "getSteamIapReceipts", dependsOnMethods = "testCreateSteamIapReceipt")
    public void testGetSteamIapReceiptByOrderId(final SteamIapReceipt receipt) {
        final var result = steamIapReceiptService.getSteamIapReceipt(receipt.getOrderId());
        assertNotNull(result);
        assertEquals(result, receipt);
    }

    @Test(dataProvider = "getSteamIapReceipts", dependsOnMethods = "testCreateSteamIapReceipt")
    public void testDisallowedUpsertForGetOrCreate(final SteamIapReceipt receipt) {
        // Attempt to overwrite using the same orderId
        final var overwrite = new SteamIapReceipt();
        overwrite.setOrderId(receipt.getOrderId());
        overwrite.setTransactionId("transId.overwrite");
        overwrite.setItemId("item.overwrite");
        overwrite.setStatus("Committed");
        overwrite.setUser(testUser);

        final var result = steamIapReceiptService.getOrCreateSteamIapReceipt(overwrite);
        // Original record must be returned — upsert not allowed
        assertEquals(result.getOrderId(), receipt.getOrderId());
    }

    @Test(dataProvider = "getSteamIapReceipts",
            dependsOnMethods = {
                    "testGetSteamIapReceiptsFromGetOrCreate",
                    "testGetSteamIapReceiptByOrderId",
                    "testDisallowedUpsertForGetOrCreate"
            })
    public void testDeleteSteamIapReceipt(final SteamIapReceipt receipt) {
        steamIapReceiptService.deleteSteamIapReceipt(receipt.getOrderId());

        try {
            final var result = steamIapReceiptService.getSteamIapReceipt(receipt.getOrderId());
            assertNull(result);
        } catch (NotFoundException e) {
            // expected
        }
    }

    public static class TestModule extends AbstractTestModule {

        @Override
        protected void configure() {
            super.configure();
            bind(SteamIapReceiptRequestInvoker.class)
                    .toInstance(mock(DefaultSteamIapReceiptRequestInvoker.class));
            bind(Client.class).toInstance(mock(Client.class));
        }

    }

}
