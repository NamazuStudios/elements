package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

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

        final TestModel testModel = new TestModel().scramble();
        final DocumentEntry<TestModel> testModelDocumentEntry = underTest.generate(testModel);

        // So the TestModel has eleven fields defined explicity, one field for the type, and finally
        // an identity field.  That's a total of 13 fields.  We should only have generated
        // exactly that may fields.

        Assert.assertEquals(testModelDocumentEntry.getDocument().getFields().size(), 14);

        // Check that the identity is sane.  We need to make sure that the identity is
        // extracted properly and that the value matches.

        final Identity<TestModel> identity = testModelDocumentEntry.getIdentity(TestModel.class);
        Assert.assertEquals(identity.getIdentityType(), String.class);
        Assert.assertEquals(identity.getDocumentType(), TestModel.class);
        Assert.assertEquals(identity.getIdentity(String.class), testModel.getId());

        // Let's check the fields in the document...

        final Fields<TestModel> fields = testModelDocumentEntry.getFields(TestModel.class);

        Assert.assertEquals(fields.getDocumentType(), TestModel.class);
        Assert.assertEquals(fields.extract(Byte.class, "byteValue"), (Byte) testModel.getByteValue());
        Assert.assertEquals(fields.extract(Character.class, "charValue"), (Character) testModel.getCharValue());
        Assert.assertEquals(fields.extract(Integer.class, "intValue"), (Integer) testModel.getIntValue());
        Assert.assertEquals(fields.extract(Long.class, "longValue"), (Long) testModel.getLongValue());
        Assert.assertEquals(fields.extract(Float.class, "floatValue"), (Float) testModel.getFloatValue());
        Assert.assertEquals(fields.extract(Double.class, "doubleValue"), (Double) testModel.getDoubleValue());
        Assert.assertEquals(fields.extract(String.class, "stringValue"), (String) testModel.getStringValue());
        Assert.assertEquals(fields.extract(String.class, "textValue"), (String) testModel.getTextValue());
        Assert.assertEquals(fields.extract(byte[].class, "blobValue"), (byte[]) testModel.getBlobValue());
        Assert.assertEquals(fields.extract(TestEnum.class, "enumValue"), (TestEnum) testModel.getEnumValue());

    }

    @Test
    private void testGenerateDocumentFromModelSubclass() {

        final TestModelSubclass testModel = new TestModelSubclass().scramble();
        final DocumentEntry<TestModelSubclass> testModelDocumentEntry = underTest.generate(testModel);

        // In addition to the above test case, this has three more fields.  An additional

        Assert.assertEquals(testModelDocumentEntry.getDocument().getFields().size(), 17);

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

    @Test
    public void testNullFieldsAreNotIndexed() {

        final TestModel testModel = new TestModel();
        testModel.setId(UUID.randomUUID().toString());

        final DocumentEntry<TestModel> testModelDocumentEntry = underTest.generate(testModel);
        Assert.assertEquals(testModelDocumentEntry.getDocument().getFields().size(), 10);

    }

    @Test
    public void testNullFieldsAreNotIndexedForSubclass() {

        final TestModelSubclass testModel = new TestModelSubclass();
        testModel.setId(UUID.randomUUID().toString());

        final DocumentEntry<TestModelSubclass> testModelDocumentEntry = underTest.generate(testModel);
        Assert.assertEquals(testModelDocumentEntry.getDocument().getFields().size(), 11);

    }

    @Test
    private void testHierarchySerialized() {

        final TestModel testModel = new TestModel();
        testModel.setId(UUID.randomUUID().toString());

        final DocumentEntry<TestModel> testModelDocumentEntry = underTest.generate(testModel);
        final Document document = testModelDocumentEntry.getDocument();

        Assert.assertEquals(document.getFields("java.class.fqn").length, 2);
        Assert.assertEquals(document.getFields("java.class.fqn")[0].stringValue(), Object.class.getName());
        Assert.assertEquals(document.getFields("java.class.fqn")[1].stringValue(), TestModel.class.getName());

    }

    @Test
    private void testHierarchySerializedForSubclass() {

        final TestModelSubclass testModel = new TestModelSubclass();
        testModel.setId(UUID.randomUUID().toString());

        final DocumentEntry<TestModelSubclass> testModelDocumentEntry = underTest.generate(testModel);
        final Document document = testModelDocumentEntry.getDocument();

        Assert.assertEquals(document.getFields("java.class.fqn").length, 3);
        Assert.assertEquals(document.getFields("java.class.fqn")[0].stringValue(), Object.class.getName());
        Assert.assertEquals(document.getFields("java.class.fqn")[1].stringValue(), TestModel.class.getName());
        Assert.assertEquals(document.getFields("java.class.fqn")[2].stringValue(), TestModelSubclass.class.getName());

    }

    @Test
    private void testGeneratorHandlesStatesProperly() {

        testGenerateDocumentFromModel();
        testGenerateDocumentFromModelSubclass();

        final UnrelatedType unrelatedType = new UnrelatedType(UUID.randomUUID().toString());
        underTest.generate(unrelatedType);

    }

    @Test
    public void testThreadSafetyStressTest() throws Exception {

        final List<Thread> threadList = new ArrayList<>();
        final AtomicBoolean failed = new AtomicBoolean(false);

        for (int i = 0; i < 500; ++i) {
            threadList.add(new Thread() {
                @Override
                public void run() {
                    try {
                        testGenerateDocumentFromModel();
                        testGenerateDocumentFromModelSubclass();
                    } catch (Exception ex) {
                        failed.set(true);
                    }
                }
            });
        }

        for (final Thread thread : threadList) {
            thread.start();
        }

        for (final Thread thread : threadList) {
            thread.join();
        }

        Assert.assertFalse(failed.get());

    }

    /**
     * Gets the object to test.
     *
     * @return
     */
    public abstract DocumentGenerator getObjectToTest();

}
