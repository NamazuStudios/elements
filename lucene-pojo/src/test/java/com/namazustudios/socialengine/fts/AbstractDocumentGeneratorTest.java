package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * This is a base test case which can be used to test customizations and implementations
 * of the {@link DocumentGenerator} interface.  This will attempt to generate a {@link Document}
 * from an instance of {@link TestModel} and {@link TestModelSubclass} and ensure the
 * existence of every field in the document.
 *
 * Created by patricktwohig on 5/31/15.
 */
public abstract class AbstractDocumentGeneratorTest {

    /**
     * The document generator under test.
     */
    private DocumentGenerator underTest;

    /**
     * Gets the object to test.
     */

    @BeforeMethod
    public void setUp() throws Exception {
        underTest = getObjectToTest();
    }

    // The document annotation from the models.  Just so we have them :)
    //@SearchableIdentity(@SearchableField(name="id", path="/id"))
    //@SearchableDocument(
    //        fields ={
    //                @SearchableField(name="byteValue", path="/byteValue"),
    //                @SearchableField(name="charValue", path="/charValue"),
    //                @SearchableField(name="intValue", path="/intValue"),
    //                @SearchableField(name="longValue", path="/longValue"),
    //                @SearchableField(name="floatValue", path="/floatValue"),
    //                @SearchableField(name="doubleValue", path="/doubleValue"),
    //                @SearchableField(name="stringValue", path="/stringValue"),
    //                @SearchableField(name="textValue", path="/textValue"),
    //                @SearchableField(name="blobValue", path="/blobValue", store = Field.Store.YES),
    //                @SearchableField(name="enumValue", path="/enumValue"),
    //        }
    //)
    //@SearchableDocument(
    //        fields = {
    //                @SearchableField(name="anotherStringValue", path="/anotherStringValue"),
    //                @SearchableField(name="yetAnotherStringValue", path="/yetAnotherStringValue"),
    //        }
    //)

    @Test
    private void testGenerateDocumentFromModel() {

        final TestModel testModel = scramble(new TestModel());
        final DocumentEntry<TestModel> testModelDocumentEntry = underTest.generate(testModel);

        // Check that the identity is sane
        final Class<TestModel> type = testModelDocumentEntry.getIdentifier(TestModel.class).getDocumentType();
        Assert.assertEquals(type, TestModel.class);

    }

    @Test
    private void testGenerateDocumentFromModelSubclass() {
        final TestModelSubclass testModel = scramble(new TestModelSubclass());
    }

    /**
     * Populates the given test model with scrambled data.
     *
     * The rationale behind this is to generate opaque data that can be extrated and
     * tested against later.
     *
     * @param testModel the test model instance
     * @return the same instance, populated with scrambled data
     */
    public TestModel scramble(final TestModel testModel) {
        final Random random = new Random();

        testModel.setId(Long.toString(System.currentTimeMillis()));
        testModel.setByteValue((byte) (0xff & random.nextInt()));
        testModel.setCharValue(Long.toString(random.nextLong()).charAt(0));
        testModel.setIntValue(random.nextInt());
        testModel.setLongValue(random.nextLong());
        testModel.setFloatValue(random.nextFloat());
        testModel.setDoubleValue(random.nextDouble());
        testModel.setStringValue(Long.toString(random.nextLong()));
        testModel.setStringValue(Long.toString(random.nextLong()) + " " + Long.toString(random.nextLong()));

        final byte[] bytes = new byte[1024];
        random.nextBytes(bytes);
        testModel.setBlobValue(bytes);

        final TestEnum[] values = TestEnum.values();
        testModel.setEnumValue(values[random.nextInt(values.length)]);

        return testModel;
    }

    /**
     * Populates the given test model with scrambled data.
     *
     * The rationale behind this is to generate opaque data that can be extrated and
     * tested against later.
     *
     * @param testModelSubclass the test model instance
     * @return the same instance, populated with scrambled data
     */
    public TestModelSubclass scramble(final TestModelSubclass testModelSubclass) {
        final Random random = new Random();

        scramble((TestModel)testModelSubclass);

        testModelSubclass.setAnotherStringValue(Long.toString(random.nextLong()));
        testModelSubclass.setYetAnotherStringValue(Long.toString(random.nextLong()));

        return testModelSubclass;

    }

    /**
     * Gets the object to test.
     *
     * @return
     */
    public abstract DocumentGenerator getObjectToTest();

}
