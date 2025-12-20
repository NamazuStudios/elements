package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.test.model.ReceiptPagination;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.model.receipt.CreateReceiptRequest;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class SuperuserReceiptApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(SuperuserReceiptApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    @Inject
    private ClientContext userClientContext;

    @Inject
    private ReceiptDao receiptDao;

    private Receipt working;

    @BeforeClass
    public void setup() {
        superUserClientContext
                .createSuperuser("receiptAdmin")
                .createSession();

        userClientContext
                .createUser("receiptUser")
                .createSession();
    }


    @Test(groups = "create")
    public void testCreateReceipt() {

        final var receipt = new CreateReceiptRequest();
        receipt.setUserId(userClientContext.getUser().getId());
        receipt.setOriginalTransactionId("transactionId.12345");
        receipt.setSchema("SuperuserReceiptApiTest");
        receipt.setPurchaseTime(currentTimeMillis());
        receipt.setBody("{\"test\": \"test\"}");

        final var response = client
                .target(apiRoot + "/receipt")
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .post(entity(receipt, APPLICATION_JSON));

        assertEquals(200, response.getStatus());

        final var createdReceipt = response.readEntity(Receipt.class);

        assertNotNull(createdReceipt);
        assertNotNull(createdReceipt.getId());
        assertEquals(receipt.getSchema(), createdReceipt.getSchema());
        assertEquals(receipt.getBody(), createdReceipt.getBody());
        assertEquals(receipt.getUserId(), createdReceipt.getUser().getId());
        assertEquals(receipt.getOriginalTransactionId(), createdReceipt.getOriginalTransactionId());
        assertEquals(receipt.getPurchaseTime(), createdReceipt.getPurchaseTime());

        working = createdReceipt;

    }


    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetReceipt() {

        final var response = client
                .target(format("%s/receipt/%s", apiRoot, working.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var metadataSpec = response.readEntity(Receipt.class);
        assertEquals(working, metadataSpec);

    }

    @Test(groups = "fetch", dependsOnGroups = "create")
    public void testGetReceipts() {

        final var userId = userClientContext.getUser().getId();
        final var search = working.getOriginalTransactionId();

        final PaginationWalker.WalkFunction<Receipt> walkFunction = (offset, count) -> {

            final var response = client
                    .target(format("%s/receipt?userId=%s&offset=%d&count=%d&search=%s",
                            apiRoot,
                            userId, offset, count, search)
                    )
                    .request()
                    .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
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
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(204, response.getStatus());

    }

    @Test(groups = "postDelete", dependsOnGroups = "delete")
    public void testDoubleDelete() {

        final var response = client
                .target(format("%s/receipt/%s", apiRoot, working.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertNotEquals(204, response.getStatus());

    }

}
