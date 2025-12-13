package dev.getelements.elements.dao.mongo.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
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

    @Test(invocationCount = INVOCATION_COUNT)
    public void testCreateReceipt(ITestContext testContext) throws JsonProcessingException {

        final int invocation = testContext.getAllTestMethods()[0].getCurrentInvocationCount();
        final var receipt = new Receipt();

        receipt.setOriginalTransactionId("id." + invocation);
        receipt.setUser(testUser);
        receipt.setSchema("dev.getelements.test_app");
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

    @Test(dataProvider = "getReceipts", dependsOnMethods = "testCreateReceipt")
    public void testGetReceiptById(final Receipt receipt) {

        final var resultReceipt = getReceiptDao().getReceipt(receipt.getId());

        assertNotNull(resultReceipt);
        assertEquals(resultReceipt, receipt);
    }

    @Test(dataProvider = "getReceipts", dependsOnMethods = "testCreateReceipt")
    public void testGetReceiptBySchemeAndTransactionId(final Receipt receipt) {

        final var resultReceipt = getReceiptDao().getReceipt(receipt.getSchema(), receipt.getOriginalTransactionId());

        assertNotNull(resultReceipt);
        assertEquals(resultReceipt, receipt);
    }

    @Test(dataProvider = "getReceipts", dependsOnMethods = "testCreateReceipt")
    public void testDisallowedUpsertForCreateReceipt(final Receipt receipt) {

        final var newReceipt = new Receipt();
        // attempt to overwrite according to the original transaction id key
        newReceipt.setOriginalTransactionId(receipt.getOriginalTransactionId());
        newReceipt.setUser(testUser);

        final var resultReceipt = getReceiptDao().createReceipt(newReceipt);

        assertEquals(resultReceipt.getOriginalTransactionId(), receipt.getOriginalTransactionId());
    }


    @Test(
            dataProvider = "getReceipts",
            dependsOnMethods = {
                    "testGetReceiptById",
                    "testGetReceiptBySchemeAndTransactionId",
                    "testDisallowedUpsertForCreateReceipt"
            },
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
