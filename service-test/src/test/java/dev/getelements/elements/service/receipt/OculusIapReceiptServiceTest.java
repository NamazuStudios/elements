package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.invoker.OculusIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;
import dev.getelements.elements.service.meta.oculusiap.UserOculusIapReceiptService;
import dev.getelements.elements.service.meta.oculusiap.invoker.DefaultOculusIapReceiptRequestInvoker;
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
    private UserOculusIapReceiptService oculusIapReceiptService;

    @Inject
    private OculusIapReceiptRequestInvoker oculusIapReceiptRequestInvoker;

    @BeforeClass
    @Override
    public void setup() {

        final var injector = createInjector(new OculusIapReceiptServiceTest.TestModule());
        injector.injectMembers(this);

        when(oculusIapReceiptRequestInvoker.invokeConsume(any(), anyString(), anyString())).then(mockInvocation -> {
            final var response = new OculusIapConsumeResponse();
            response.setSuccess(true);
            return response;
        });

        when(oculusIapReceiptRequestInvoker.invokeVerify(any(), anyString(), anyString())).then(mockInvocation -> {
            final OculusIapReceipt receipt = mockInvocation.getArgument(0);
            final var response = new OculusIapVerifyReceiptResponse();
            response.setSuccess(true);
            response.setGrantTime(receipt.getGrantTime());
            return response;
        });

        super.setup();
    }

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateOculusIapReceipt(ITestContext testContext) {

        final var oculusIapReceipt = getOculusIapReceipt(testContext);

        try {
            oculusIapReceiptService.getOculusIapReceipt(oculusIapReceipt.getPurchaseId());
        } catch (Exception e) {
            // this is the expected result
        }

        final var resultOculusIapReceipt = oculusIapReceiptService.getOrCreateOculusIapReceipt(oculusIapReceipt);

        assertNotNull(resultOculusIapReceipt);

        assertEquals(oculusIapReceipt.getPurchaseId(), resultOculusIapReceipt.getPurchaseId());
        assertEquals(oculusIapReceipt.getSku(), resultOculusIapReceipt.getSku());
        assertEquals(oculusIapReceipt.getUserId(), resultOculusIapReceipt.getUserId());
        assertEquals(oculusIapReceipt.getReportingId(), resultOculusIapReceipt.getReportingId());
        assertEquals(oculusIapReceipt.getDeveloperPayload(), resultOculusIapReceipt.getDeveloperPayload());
        assertEquals(oculusIapReceipt.getExpirationTime(), resultOculusIapReceipt.getExpirationTime());
        assertEquals(oculusIapReceipt.getGrantTime(), resultOculusIapReceipt.getGrantTime());
    }

    @NotNull
    private OculusIapReceipt getOculusIapReceipt(ITestContext testContext) {

        final var invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();
        final var oculusIapReceipt = new OculusIapReceipt();
        oculusIapReceipt.setUserId("fbUserId." + invocation);
        oculusIapReceipt.setSku("sku." + invocation);
        oculusIapReceipt.setPurchaseId("purchaseId." + invocation);
        oculusIapReceipt.setGrantTime(currentTimeMillis());
        oculusIapReceipt.setExpirationTime(currentTimeMillis() + 10000L * invocation);
        oculusIapReceipt.setReportingId("reportingId." + invocation);
        oculusIapReceipt.setDeveloperPayload("developerPayload." + invocation);

        return oculusIapReceipt;
    }

    @DataProvider
    public Object[][] getOculusIapReceipts() {

        final Object[][] objects = oculusIapReceiptService
                .getOculusIapReceipts(0, 20)
                .getObjects()
                .stream()
                .map(ri -> new Object[]{ri})
                .toArray(Object[][]::new);

        assertTrue(objects.length > 0);

        return objects;
    }

    @Test(dataProvider = "getOculusIapReceipts", dependsOnMethods = "testCreateOculusIapReceipt")
    public void testGetOculusIapReceiptsFromGetOrCreateOculusIapReceipt(final OculusIapReceipt oculusIapReceipt) {

        final var resultOculusIapReceipt = oculusIapReceiptService.getOrCreateOculusIapReceipt(oculusIapReceipt);

        assertNotNull(resultOculusIapReceipt);
        assertEquals(resultOculusIapReceipt, oculusIapReceipt);
    }

    @Test(dataProvider = "getOculusIapReceipts", dependsOnMethods = "testCreateOculusIapReceipt")
    public void testGetOculusIapReceiptByOriginalTransactionId(final OculusIapReceipt oculusIapReceipt) {

        final var resultOculusIapReceipt = oculusIapReceiptService.getOculusIapReceipt(oculusIapReceipt.getPurchaseId());

        assertNotNull(resultOculusIapReceipt);
        assertEquals(resultOculusIapReceipt, oculusIapReceipt);
    }

    @Test(dataProvider = "getOculusIapReceipts", dependsOnMethods = "testCreateOculusIapReceipt")
    public void testDisallowedUpsertForGetOrCreateOculusIapReceipt(final OculusIapReceipt oculusIapReceipt) {

        final var newOculusIapReceipt = new OculusIapReceipt();
        newOculusIapReceipt.setPurchaseId(oculusIapReceipt.getPurchaseId());

        // attempt to overwrite according to the original transaction id key
        newOculusIapReceipt.setUserId("fbUserId." + -1);
        newOculusIapReceipt.setSku("sku." + -1);
        newOculusIapReceipt.setGrantTime(currentTimeMillis());
        newOculusIapReceipt.setExpirationTime(currentTimeMillis() - 10000L);
        newOculusIapReceipt.setReportingId("reportingId." + -1);
        newOculusIapReceipt.setDeveloperPayload("developerPayload." + -1);

        final var resultOculusIapReceipt = oculusIapReceiptService.getOrCreateOculusIapReceipt(newOculusIapReceipt);

        assertEquals(resultOculusIapReceipt.getPurchaseId(), oculusIapReceipt.getPurchaseId());
    }

    @Test(dataProvider = "getOculusIapReceipts",
            dependsOnMethods = {
                    "testGetOculusIapReceiptsFromGetOrCreateOculusIapReceipt",
                    "testGetOculusIapReceiptByOriginalTransactionId",
                    "testDisallowedUpsertForGetOrCreateOculusIapReceipt"
            })
    public void testDeleteOculusIapReceipt(final OculusIapReceipt oculusIapReceipt) {

        oculusIapReceiptService.deleteOculusIapReceipt(oculusIapReceipt.getPurchaseId());

        try {
            final var resultOculusIapReceipt = oculusIapReceiptService.getOculusIapReceipt(oculusIapReceipt.getPurchaseId());
            assertNull(resultOculusIapReceipt);
        }
        catch (NotFoundException e) {
            // this is the expected result
        }
    }

    public static class TestModule extends AbstractTestModule {

        @Override
        protected void configure() {

            bind(OculusIapReceiptRequestInvoker.class).toInstance(mock(DefaultOculusIapReceiptRequestInvoker.class));
            bind(Client.class).toInstance(mock(Client.class));
            super.configure();
        }

    }

}
