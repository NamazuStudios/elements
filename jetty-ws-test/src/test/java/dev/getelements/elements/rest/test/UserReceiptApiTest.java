package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.test.model.ReceiptPagination;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.PROFILE_ID;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService.APPLE_IAP_SCHEME;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class UserReceiptApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(UserReceiptApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext userClientContext;

    @Inject
    private ReceiptDao receiptDao;

    private Receipt working;

    @BeforeClass
    public void setup() {
        userClientContext
                .createUser("receiptUser")
                .createProfile("receiptUser")
                .createSession();

        working = new Receipt();
        working.setUser(userClientContext.getUser());
        working.setOriginalTransactionId("transactionId.12345");
        working.setSchema(APPLE_IAP_SCHEME);
        working.setPurchaseTime(currentTimeMillis());
        working.setBody("{ \"test\": \"test\" }");

        receiptDao.createReceipt(working);
    }


    @Test(groups = "create")
    public void testCreateReceipt() {

        final var response = client
                .target(apiRoot + "/receipt")
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .header(PROFILE_ID, userClientContext.getDefaultProfile().getId())
                .post(entity(working, APPLICATION_JSON));

        assertEquals(401, response.getStatus());

    }


    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetReceipt() {

        final var response = client
                .target(format("%s/receipt/%s", apiRoot, working.getId()))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .get();

        assertEquals(200, response.getStatus());

        final var metadataSpec = response.readEntity(Receipt.class);
        assertEquals(working, metadataSpec);

    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetReceipts() {

        final PaginationWalker.WalkFunction<Receipt> walkFunction = (offset, count) -> {

            final var response = client
                    .target(format("%s/receipt?offset=%d&count=%d",
                            apiRoot,
                            offset, count)
                    )
                    .request()
                    .header(SESSION_SECRET, userClientContext.getSessionSecret())
                    .get();

            assertEquals(200, response.getStatus());
            return response.readEntity(ReceiptPagination.class);

        };

        final var specs = new PaginationWalker().toList(walkFunction);
        assertTrue(specs.contains(working));
    }

    @Test(groups = "delete", dependsOnGroups = "fetch")
    public void testDeleteReceipt() {

        final var response = client
                .target(format("%s/receipt/%s", apiRoot, working.getId()))
                .request()
                .header(SESSION_SECRET, userClientContext.getSessionSecret())
                .delete();

        assertEquals(401, response.getStatus());

    }


}
