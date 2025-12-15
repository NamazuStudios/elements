package dev.getelements.elements.dao.mongo.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoReceiptDaoTest {

    private static final int INVOCATION_COUNT = 10;

    private UserDao userDao;

    private ReceiptDao receiptDao;

    private User testUser;

    private UserTestFactory userTestFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> testBody = Map.of( "testKey", "testValue" );

    @BeforeClass
    public void createTestUser() {
        testUser = getUserTestFactory().createTestUser();
    }

    @Test(invocationCount = INVOCATION_COUNT, groups = "create")
    public void testCreateReceipt(ITestContext testContext) throws JsonProcessingException {

        final int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();
        final var receipt = new Receipt();

        receipt.setOriginalTransactionId("id." + invocation + "." + UUID.randomUUID());
        receipt.setUser(testUser);
        receipt.setSchema("dev.getelements.test_receipt_schema." + invocation);
        receipt.setPurchaseTime(new Date().getTime());
        receipt.setBody(objectMapper.writeValueAsString(testBody));

        try {
            getReceiptDao().getReceipt(receipt.getOriginalTransactionId());
        } catch (NotFoundException e) {
            // this is the expected result
        }

        final var resultReceipt = getReceiptDao().createReceipt(receipt);

        assertNotNull(resultReceipt);

        //White spaces introduced by mapper caused assertion failure, so we manually remap here to compare
        final var convertedResultBody = objectMapper.readValue(resultReceipt.getBody(), Map.class);
        final var convertedSourceBody = objectMapper.readValue(receipt.getBody(), Map.class);

        assertEquals(resultReceipt.getOriginalTransactionId(), receipt.getOriginalTransactionId());
        assertEquals(resultReceipt.getUser(), receipt.getUser());
        assertEquals(convertedResultBody, convertedSourceBody);
        assertEquals(resultReceipt.getPurchaseTime(), receipt.getPurchaseTime());
        assertEquals(resultReceipt.getSchema(), receipt.getSchema());
    }

    @DataProvider
    public Object[][] getReceipts() {

        final Object[][] objects = getReceiptDao()
                .getReceipts(testUser, 0, 20)
                .getObjects()
                .stream()
                .map(ri -> new Object[]{ri})
                .toArray(Object[][]::new);

        assertTrue(objects.length > 0);

        return objects;
    }

    @Test(dataProvider = "getReceipts", groups = "get", dependsOnGroups = "create")
    public void testGetReceiptById(final Receipt receipt) {

        final var resultReceipt = getReceiptDao().getReceipt(receipt.getId());

        assertNotNull(resultReceipt);
        assertEquals(resultReceipt, receipt);
    }

    @Test(dataProvider = "getReceipts", groups = "get", dependsOnGroups = "create")
    public void testGetReceiptsBySchemaFilter(final Receipt receipt) {

        final var count = 20;
        final var offset = 0;
        final var resultReceiptPagination = getReceiptDao().getReceipts(receipt.getUser(), 0, 20, receipt.getSchema());

        assertNotNull(resultReceiptPagination);
        assertTrue(resultReceiptPagination.getTotal() <= count);
        assertEquals(resultReceiptPagination.getOffset(), offset);
        assertFalse(resultReceiptPagination.getObjects().isEmpty());
        // Each schema is unique so there should only ever be one result
        assertEquals(resultReceiptPagination.stream().count(), 1);
        final var resultReceiptOptional = resultReceiptPagination.stream().findFirst();
        assertTrue(resultReceiptOptional.isPresent());
        final var resultReceipt = resultReceiptOptional.get();
        assertEquals(resultReceipt, receipt);
    }

    @Test(dataProvider = "getReceipts", groups = "get", dependsOnGroups = "create")
    public void testGetReceiptsByPartialSchemaFilter(final Receipt receipt) {

        final var count = 20;
        final var offset = 0;
        final var search = receipt.getSchema().substring(0, receipt.getSchema().length() - 2);
        final var resultReceiptPagination = getReceiptDao().getReceipts(receipt.getUser(), 0, 20, search);

        assertNotNull(resultReceiptPagination);
        assertTrue(resultReceiptPagination.getTotal() <= count);
        assertEquals(resultReceiptPagination.getOffset(), offset);
        assertFalse(resultReceiptPagination.getObjects().isEmpty());
        // We should find multiple with a partial search
        assertEquals(resultReceiptPagination.stream().count(), INVOCATION_COUNT);
    }


    @Test(dataProvider = "getReceipts", groups = "get", dependsOnGroups = "create")
    public void testGetReceiptsByTransactionIdFilter(final Receipt receipt) {

        final var count = 20;
        final var offset = 0;
        final var resultReceiptPagination = getReceiptDao().getReceipts(receipt.getUser(), 0, 20, receipt.getOriginalTransactionId());

        assertNotNull(resultReceiptPagination);
        assertTrue(resultReceiptPagination.getTotal() <= count);
        assertEquals(resultReceiptPagination.getOffset(), offset);
        assertFalse(resultReceiptPagination.getObjects().isEmpty());
        // Each schema is unique so there should only ever be one result
        assertEquals(resultReceiptPagination.stream().count(), 1);
        final var resultReceiptOptional = resultReceiptPagination.stream().findFirst();
        assertTrue(resultReceiptOptional.isPresent());
        final var resultReceipt = resultReceiptOptional.get();
        assertEquals(resultReceipt, receipt);
    }

    @Test(dataProvider = "getReceipts", groups = "get", dependsOnGroups = "create")
    public void testGetReceiptsByPartialTransactionIdFilter(final Receipt receipt) {

        final var count = 20;
        final var offset = 0;
        final var search = receipt.getOriginalTransactionId().substring(0, receipt.getOriginalTransactionId().length() - 2);
        final var resultReceiptPagination = getReceiptDao().getReceipts(receipt.getUser(), 0, 20, search);

        assertNotNull(resultReceiptPagination);
        assertTrue(resultReceiptPagination.getTotal() <= count);
        assertEquals(resultReceiptPagination.getOffset(), offset);
        assertFalse(resultReceiptPagination.getObjects().isEmpty());
        // Transaction id is a bit more unique so we don't expect to find more than 1
        assertTrue(resultReceiptPagination.getTotal() >= 1 && resultReceiptPagination.getTotal() < INVOCATION_COUNT);
    }


    @Test(dataProvider = "getReceipts", groups = "get", dependsOnGroups = "create")
    public void testGetReceiptBySchemeAndTransactionId(final Receipt receipt) {

        final var resultReceipt = getReceiptDao().getReceipt(receipt.getSchema(), receipt.getOriginalTransactionId());

        assertNotNull(resultReceipt);
        assertEquals(resultReceipt, receipt);
    }

    @Test(dataProvider = "getReceipts", groups = "get", dependsOnGroups = "create")
    public void testDisallowedUpsertForCreateReceipt(final Receipt receipt) {

        // Attempt to overwrite according to the original transaction id key and schema.
        // Should return the existing receipt instead.
        final var newReceipt = new Receipt();
        newReceipt.setOriginalTransactionId(receipt.getOriginalTransactionId());
        newReceipt.setSchema(receipt.getSchema());
        newReceipt.setUser(testUser);

        final var resultReceipt = getReceiptDao().createReceipt(newReceipt);

        assertEquals(resultReceipt, receipt);
    }


    @Test(
            dataProvider = "getReceipts",
            groups = "delete", dependsOnGroups = "get",
            expectedExceptions = NotFoundException.class
    )
    public void testDeleteReceipt(final Receipt receipt) {

        getReceiptDao().deleteReceipt(receipt.getOriginalTransactionId());

        final Receipt resultReceipt = getReceiptDao().getReceipt(receipt.getOriginalTransactionId());
        assertNull(resultReceipt);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ReceiptDao getReceiptDao() {
        return receiptDao;
    }

    @Inject
    public void setReceiptDao(ReceiptDao receiptDao) {
        this.receiptDao = receiptDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

}
