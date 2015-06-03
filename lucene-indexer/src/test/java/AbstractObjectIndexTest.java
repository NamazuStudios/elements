import com.namazustudios.socialengine.fts.*;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

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

    @Test
    public void testWildcardPolymorphicQuery() throws Exception {

        // Indexes a test model instance

        final TestModel testModel = new TestModel().scramble();
        underTest.index(testModel);

        final TestModelSubclass testModelSubclass = new TestModelSubclass().scramble();
        underTest.index(testModelSubclass);

        final UnrelatedType unrelatedType = new UnrelatedType(UUID.randomUUID().toString());
        underTest.index(unrelatedType);

        final Set<String> ids = new HashSet<>();

        ids.add(testModel.getId());
        ids.add(testModelSubclass.getId());

        final Set<Class<?>> types = new HashSet<>();
        types.add(TestModel.class);
        types.add(TestModelSubclass.class);

        try (final TopDocsSearchResult<TestModel> result = underTest.executeQueryForType(TestModel.class)
                                                                    .withTopScores(100)) {

            for (final ScoredDocumentEntry<TestModel> testModelScoredDocumentEntry : result) {

                final Identity<TestModel> identity = testModelScoredDocumentEntry.getIdentity(TestModel.class);

                Assert.assertTrue(ids.remove(identity.getIdentity()));
                Assert.assertTrue(types.remove(identity.getDocumentType()));

            }

        }

        Assert.assertTrue(ids.isEmpty());
        Assert.assertTrue(types.isEmpty());

    }

    @Test
    public void testArbitraryQuery() {

        final Random random = new Random();

        final TestModel needle = new TestModel().scramble();
        needle.setTextValue("needle");
        underTest.index(needle);

        for (int i = 0; i < 1000; ++i) {
            final TestModel haystack =
                random.nextBoolean() ? new TestModel().scramble() :
                                       new TestModelSubclass().scramble();

            haystack.setTextValue("haystack");

            underTest.index(haystack);
        }

        final Query query = new TermQuery(new Term("textValue", "needle"));

        try (final TopDocsSearchResult<TestModel> result = underTest.executeQueryForObjects(TestModel.class, query)
                                                                    .withTopScores(100)) {

            final Iterator<ScoredDocumentEntry<TestModel>> iterator = result.iterator();
            final ScoredDocumentEntry<TestModel> entry = iterator.next();

            Assert.assertEquals(entry.getIdentity(TestModel.class).getIdentity(), needle.getId());
            Assert.assertFalse(iterator.hasNext());

        }

    }

    @Test
    public void testPagination() {

        final Set<String> idSet = new HashSet<>();
        final List<String> idList = new ArrayList<>();

        for (int i = 0; i < 1000; ++i) {
            final TestModel model = new TestModel().scramble();
            idList.add(model.getId());
            underTest.index(model);
        }

        idSet.addAll(idList);

        for (int i = 0; i < 1000; i+=10) {
            try (final TopDocsSearchResult<TestModel> result = underTest.executeQueryForType(TestModel.class)
                                                                        .withTopScores(i + 10)
                                                                        .after(i, 10)) {

                for (final ScoredDocumentEntry<TestModel> testModelScoredDocumentEntry : result) {
                    final Identity<TestModel> identity = testModelScoredDocumentEntry.getIdentity(TestModel.class);
                    Assert.assertTrue(idSet.remove(identity.getIdentity()));
                }

            }
        }

    }

}
