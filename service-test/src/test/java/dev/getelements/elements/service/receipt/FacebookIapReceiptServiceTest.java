package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.meta.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.invoker.FacebookIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.model.FacebookIapVerifyReceiptResponse;
import dev.getelements.elements.service.meta.facebookiap.UserFacebookIapReceiptService;
import dev.getelements.elements.service.meta.facebookiap.invoker.DefaultFacebookIapReceiptRequestInvoker;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import org.jetbrains.annotations.NotNull;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.google.inject.Guice.createInjector;
import static java.lang.System.currentTimeMillis;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;


public class OculusIapReceiptServiceTest extends AbstractReceiptServiceTest {

    @Inject
    private UserFacebookIapReceiptService facebookIapReceiptService;

    @Inject
    private FacebookIapReceiptRequestInvoker facebookIapReceiptRequestInvoker;

    @BeforeClass
    @Override
    public void setup() {

        final var injector = createInjector(new OculusIapReceiptServiceTest.TestModule());
        injector.injectMembers(this);

        when(facebookIapReceiptRequestInvoker.invokeConsume(any(), anyString(), anyString())).then(mockInvocation -> {
            final var response = new FacebookIapConsumeResponse();
            response.setSuccess(true);
            return response;
        });

        when(facebookIapReceiptRequestInvoker.invokeVerify(any(), anyString(), anyString())).then(mockInvocation -> {
            final FacebookIapReceipt receipt = mockInvocation.getArgument(0);
            final var response = new FacebookIapVerifyReceiptResponse();
            response.setSuccess(true);
            response.setGrantTime(receipt.getGrantTime());
            return response;
        });

        super.setup();
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateFacebookIapReceipt(ITestContext testContext) {

        final var facebookIapReceipt = getFacebookIapReceipt(testContext);

        try {
            facebookIapReceiptService.getFacebookIapReceipt(facebookIapReceipt.getPurchaseId());
        } catch (Exception e) {
            // this is the expected result
        }

        final var resultFacebookIapReceipt = facebookIapReceiptService.getOrCreateFacebookIapReceipt(facebookIapReceipt);

        assertNotNull(resultFacebookIapReceipt);

        assertEquals(facebookIapReceipt.getPurchaseId(), resultFacebookIapReceipt.getPurchaseId());
        assertEquals(facebookIapReceipt.getSku(), resultFacebookIapReceipt.getSku());
        assertEquals(facebookIapReceipt.getUserId(), resultFacebookIapReceipt.getUserId());
        assertEquals(facebookIapReceipt.getReportingId(), resultFacebookIapReceipt.getReportingId());
        assertEquals(facebookIapReceipt.getDeveloperPayload(), resultFacebookIapReceipt.getDeveloperPayload());
        assertEquals(facebookIapReceipt.getExpirationTime(), resultFacebookIapReceipt.getExpirationTime());
        assertEquals(facebookIapReceipt.getGrantTime(), resultFacebookIapReceipt.getGrantTime());
    }

    @NotNull
    private FacebookIapReceipt getFacebookIapReceipt(ITestContext testContext) {

        final var invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();
        final var facebookIapReceipt = new FacebookIapReceipt();
        facebookIapReceipt.setFbUserId("fbUserId." + invocation);
        facebookIapReceipt.setSku("sku." + invocation);
        facebookIapReceipt.setPurchaseId("purchaseId." + invocation);
        facebookIapReceipt.setGrantTime(currentTimeMillis());
        facebookIapReceipt.setExpirationTime(currentTimeMillis() + 10000L * invocation);
        facebookIapReceipt.setReportingId("reportingId." + invocation);
        facebookIapReceipt.setDeveloperPayload("developerPayload." + invocation);

        return facebookIapReceipt;
    }

    @DataProvider
    public Object[][] getFacebookIapReceipts() {

        final Object[][] objects = facebookIapReceiptService
                .getFacebookIapReceipts(0, 20)
                .getObjects()
                .stream()
                .map(ri -> new Object[]{ri})
                .toArray(Object[][]::new);

        assertTrue(objects.length > 0);

        return objects;
    }

    @Test(dataProvider = "getFacebookIapReceipts", dependsOnMethods = "testCreateFacebookIapReceipt")
    public void testGetFacebookIapReceiptsFromGetOrCreateFacebookIapReceipt(final FacebookIapReceipt facebookIapReceipt) {

        final var resultFacebookIapReceipt = facebookIapReceiptService.getOrCreateFacebookIapReceipt(facebookIapReceipt);

        assertNotNull(resultFacebookIapReceipt);
        assertEquals(resultFacebookIapReceipt, facebookIapReceipt);
    }

    @Test(dataProvider = "getFacebookIapReceipts", dependsOnMethods = "testCreateFacebookIapReceipt")
    public void testGetFacebookIapReceiptByOriginalTransactionId(final FacebookIapReceipt facebookIapReceipt) {

        final var resultFacebookIapReceipt = facebookIapReceiptService.getFacebookIapReceipt(facebookIapReceipt.getPurchaseId());

        assertNotNull(resultFacebookIapReceipt);
        assertEquals(resultFacebookIapReceipt, facebookIapReceipt);
    }

    @Test(dataProvider = "getFacebookIapReceipts", dependsOnMethods = "testCreateFacebookIapReceipt")
    public void testDisallowedUpsertForGetOrCreateFacebookIapReceipt(final FacebookIapReceipt facebookIapReceipt) {

        final var newFacebookIapReceipt = new FacebookIapReceipt();
        newFacebookIapReceipt.setPurchaseId(facebookIapReceipt.getPurchaseId());

        // attempt to overwrite according to the original transaction id key
        newFacebookIapReceipt.setFbUserId("fbUserId." + -1);
        newFacebookIapReceipt.setSku("sku." + -1);
        newFacebookIapReceipt.setGrantTime(currentTimeMillis());
        newFacebookIapReceipt.setExpirationTime(currentTimeMillis() - 10000L);
        newFacebookIapReceipt.setReportingId("reportingId." + -1);
        newFacebookIapReceipt.setDeveloperPayload("developerPayload." + -1);

        final var resultFacebookIapReceipt = facebookIapReceiptService.getOrCreateFacebookIapReceipt(newFacebookIapReceipt);

        assertEquals(resultFacebookIapReceipt.getPurchaseId(), facebookIapReceipt.getPurchaseId());
    }

    @Test(dataProvider = "getFacebookIapReceipts",
            dependsOnMethods = {
                    "testGetFacebookIapReceiptsFromGetOrCreateFacebookIapReceipt",
                    "testGetFacebookIapReceiptByOriginalTransactionId",
                    "testDisallowedUpsertForGetOrCreateFacebookIapReceipt"
            })
    public void testDeleteFacebookIapReceipt(final FacebookIapReceipt facebookIapReceipt) {

        facebookIapReceiptService.deleteFacebookIapReceipt(facebookIapReceipt.getPurchaseId());

        try {
            final var resultFacebookIapReceipt = facebookIapReceiptService.getFacebookIapReceipt(facebookIapReceipt.getPurchaseId());
            assertNull(resultFacebookIapReceipt);
        }
        catch (NotFoundException e) {
            // this is the expected result
        }
    }

    public static class TestModule extends AbstractReceiptServiceTest.AbstractTestModule {

        @Override
        protected void configure() {

            bind(FacebookIapReceiptRequestInvoker.class).toInstance(mock(DefaultFacebookIapReceiptRequestInvoker.class));
            bind(Client.class).toInstance(mock(Client.class));
            super.configure();
        }

    }

}
