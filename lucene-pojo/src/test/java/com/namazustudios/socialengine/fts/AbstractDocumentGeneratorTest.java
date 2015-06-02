package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
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

    @Test
    private void testGenerateDocumentFromModel() {

        final TestModel testModel = scramble(new TestModel());
        final DocumentEntry<TestModel> testModelDocumentEntry = underTest.generate(testModel);

        // So the TestModel has eleven fields defined explicity, one field for the type, and finally
        // an identity field.  That's a total of 13 fields.  We should only have generated
        // exactly that may fields.

        Assert.assertEquals(testModelDocumentEntry.getDocument().getFields().size(), 13);

        // Check that the identity is sane.  We need to make sure that the identity is
        // extracted properly and that the value matches.

        final Identity<TestModel> identity = testModelDocumentEntry.getIdentity(TestModel.class);
        Assert.assertEquals(identity.getIdentityType(), String.class);
        Assert.assertEquals(identity.getDocumentType(), TestModel.class);
        Assert.assertEquals(identity.getIdentity(String.class), testModel.getId());

        // Let's check the fields in the document...

        final Fields<TestModel> fields = testModelDocumentEntry.getFields(TestModel.class);

        Assert.assertEquals(fields.getDocumentType(), TestModel.class);
        Assert.assertEquals(fields.extract(Byte.class, "byteValue"), (Byte)testModel.getByteValue());
        Assert.assertEquals(fields.extract(Character.class, "charValue"), (Character)testModel.getCharValue());
        Assert.assertEquals(fields.extract(Integer.class, "intValue"), (Integer)testModel.getIntValue());
        Assert.assertEquals(fields.extract(Long.class, "longValue"), (Long)testModel.getLongValue());
        Assert.assertEquals(fields.extract(Float.class, "floatValue"), (Float)testModel.getFloatValue());
        Assert.assertEquals(fields.extract(Double.class, "doubleValue"), (Double) testModel.getDoubleValue());
        Assert.assertEquals(fields.extract(String.class, "stringValue"), (String) testModel.getStringValue());
        Assert.assertEquals(fields.extract(String.class, "textValue"), (String) testModel.getTextValue());
        Assert.assertEquals(fields.extract(byte[].class, "blobValue"), (byte[]) testModel.getBlobValue());
        Assert.assertEquals(fields.extract(TestEnum.class, "enumValue"), (TestEnum) testModel.getEnumValue());

    }

    @Test
    private void testGenerateDocumentFromModelSubclass() {

        final TestModelSubclass testModel = scramble(new TestModelSubclass());
        final DocumentEntry<TestModelSubclass> testModelDocumentEntry = underTest.generate(testModel);

        // In addition to the above test case, this has three more fields.  An additional

        Assert.assertEquals(testModelDocumentEntry.getDocument().getFields().size(), 16);

        // Check that the identity is sane.  We need to make sure that the identity is
        // extracted properly and that the value matches.  This is a littie bit more complicated
        // with a subclass becuase we need to make sure that it always returns the most
        // specific type in the type hierarchy.

        final Identity<TestModelSubclass> identity = testModelDocumentEntry.getIdentity(TestModelSubclass.class);
        Assert.assertEquals(identity.getIdentityType(), String.class);
        Assert.assertEquals(identity.getDocumentType(), TestModelSubclass.class);
        Assert.assertEquals(identity.getIdentity(String.class), testModel.getId());

        final DocumentEntry<TestModel> entryAsTestModel = testModelDocumentEntry.as(TestModel.class);
        final Identity<TestModel> testModelIdentity = entryAsTestModel.getIdentity(TestModel.class);
        Assert.assertEquals(testModelIdentity.getIdentityType(), String.class);
        Assert.assertEquals(testModelIdentity.getDocumentType(), TestModelSubclass.class);
        Assert.assertEquals(testModelIdentity.getIdentity(String.class), testModel.getId());

        // Let's check the fields in the document...

        final Fields<TestModelSubclass> fields = testModelDocumentEntry.getFields(TestModelSubclass.class);
        Assert.assertEquals(fields.getDocumentType(), TestModelSubclass.class);

        Assert.assertEquals(fields.extract(Byte.class, "byteValue"), (Byte)testModel.getByteValue());
        Assert.assertEquals(fields.extract(Character.class, "charValue"), (Character)testModel.getCharValue());
        Assert.assertEquals(fields.extract(Integer.class, "intValue"), (Integer)testModel.getIntValue());
        Assert.assertEquals(fields.extract(Long.class, "longValue"), (Long)testModel.getLongValue());
        Assert.assertEquals(fields.extract(Float.class, "floatValue"), (Float)testModel.getFloatValue());
        Assert.assertEquals(fields.extract(Double.class, "doubleValue"), (Double) testModel.getDoubleValue());
        Assert.assertEquals(fields.extract(String.class, "stringValue"), (String) testModel.getStringValue());
        Assert.assertEquals(fields.extract(String.class, "textValue"), (String) testModel.getTextValue());
        Assert.assertEquals(fields.extract(byte[].class, "blobValue"), (byte[]) testModel.getBlobValue());
        Assert.assertEquals(fields.extract(TestEnum.class, "enumValue"), (TestEnum) testModel.getEnumValue());

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
        testModel.setShortValue((short)random.nextInt());
        testModel.setIntValue(random.nextInt());
        testModel.setLongValue(random.nextLong());
        testModel.setFloatValue(random.nextFloat());
        testModel.setDoubleValue(random.nextDouble());
        testModel.setStringValue(Long.toString(random.nextLong()));
        testModel.setStringValue(Long.toString(random.nextLong()) + " " + Long.toString(random.nextLong()));
        testModel.setTextValue(Long.toString(random.nextLong()) + " " + Long.toString(random.nextLong()));

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
