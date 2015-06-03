import com.namazustudios.socialengine.fts.*;
import org.apache.lucene.search.IndexSearcher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.UUID;

/**
 * Created by patricktwohig on 6/1/15.
 */
public abstract class AbstractObjectIndexTest {

    private ObjectIndex underTest;

    @BeforeMethod
    public void setUp() throws Exception {
        underTest = getObjectToTest();
    }

    public abstract ObjectIndex getObjectToTest() throws Exception;

    @Test
    public void testIndex() throws Exception {

        // Indexes a test model instance

        final TestModel testModel = new TestModel().scramble();
        underTest.index(testModel);

        try (final IOContext<IndexSearcher> indexReaderIOContext = underTest.getIndexSearcherContextProvider().get()) {
            Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), 1);
        }

    }

    @Test
    public void testIndexUpdatesObjectWithSameId() throws Exception {

        final String objectId = UUID.randomUUID().toString();
        final TestModel testModel = new TestModel().scramble(objectId);

        underTest.index(testModel);

        try (final IOContext<IndexSearcher> indexReaderIOContext = underTest.getIndexSearcherContextProvider().get()) {
            Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), 1);
        }

        testModel.scramble(objectId);
        underTest.index(testModel);

        try (final IOContext<IndexSearcher> indexReaderIOContext = underTest.getIndexSearcherContextProvider().get()) {
            Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), 1);
        }

    }

    @Test
    public void testIndexMultipleDocumentsWithUniqueIds() throws Exception {

        for (int i = 0; i < 100; ++i) {

            final TestModel testModel = new TestModel().scramble();

            underTest.index(testModel);

            try (final IOContext<IndexSearcher> indexReaderIOContext =
                 underTest.getIndexSearcherContextProvider().get()) {

                Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), i + 1);

            }

        }

    }

    @Test
    public void testCreateAndDeleteByObject() throws Exception {

        // Indexes a test model instance

        final TestModel testModel = new TestModel().scramble();

        underTest.index(testModel);
        underTest.index(new TestModel().scramble());

        try (final IOContext<IndexSearcher> indexReaderIOContext = underTest.getIndexSearcherContextProvider().get()) {
            Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), 2);
        }

        underTest.delete(testModel);

        try (final IOContext<IndexSearcher> indexReaderIOContext = underTest.getIndexSearcherContextProvider().get()) {
            Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), 1);
        }

    }

    @Test
    public void testCreateAndDeleteByTypeAndIdentifier() throws Exception {

        // Indexes a test model instance

        final String objectId = UUID.randomUUID().toString();

        underTest.index(new TestModel().scramble());
        underTest.index(new TestModel().scramble(objectId));

        try (final IOContext<IndexSearcher> indexReaderIOContext = underTest.getIndexSearcherContextProvider().get()) {
            Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), 2);
        }

        underTest.delete(TestModel.class, objectId);

        try (final IOContext<IndexSearcher> indexReaderIOContext = underTest.getIndexSearcherContextProvider().get()) {
            Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), 1);
        }

        underTest.delete(TestModel.class, UUID.randomUUID().toString());

        try (final IOContext<IndexSearcher> indexReaderIOContext = underTest.getIndexSearcherContextProvider().get()) {
            Assert.assertEquals(indexReaderIOContext.instance().getIndexReader().numDocs(), 1);
        }

    }

    @Test
    public void testWildcardQuery() throws Exception {

        // Indexes a test model instance

        final TestModel testModel = new TestModel().scramble();
        underTest.index(testModel);


        try (final TopDocsSearchResult<TestModel> result = underTest.executeQueryForType(TestModel.class)
                                                                    .withTopScores(100)) {

            final Iterator<ScoredDocumentEntry<TestModel>> iterator = result.iterator();
            final ScoredDocumentEntry<TestModel> entry = iterator.next();

            Assert.assertEquals(entry.getIdentity(TestModel.class).getIdentity(), testModel.getId());
            Assert.assertEquals(entry.getIdentity(TestModel.class).getDocumentType(), TestModel.class);

            Assert.assertFalse(iterator.hasNext());

        }

    }

}
