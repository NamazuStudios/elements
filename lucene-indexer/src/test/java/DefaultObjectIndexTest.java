import com.namazustudios.socialengine.fts.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Tests the {@link DefaultObjectIndexTest} using an instance of {@link RAMDirectory}
 * and {@link StandardAnalyzer}.
 *
 * Created by patricktwohig on 6/1/15.
 */
public class DefaultObjectIndexTest extends AbstractObjectIndexTest {

    private final AtomicInteger writerOpens = new AtomicInteger(0);

    private final AtomicInteger readerOpens = new AtomicInteger(0);

    @BeforeTest
    public void resetOpenCloseCount() {
        writerOpens.set(0);
        readerOpens.set(0);
    }

    @AfterTest
    public void checkOpenCloseCount() {
        Assert.assertEquals(writerOpens.get(), 0);
        Assert.assertEquals(readerOpens.get(), 0);
    }

    @Override
    public ObjectIndex getObjectToTest() throws IOException {

        final Directory directory = new RAMDirectory();
        createSearchIndex(directory);

        final IOContext.Provider<IndexWriter> indexWriterProvider = new IOContext.Provider<IndexWriter>() {
            @Override
            public IOContext get() throws IOException {

                final IndexWriterConfig indexWriterConfig =
                        new IndexWriterConfig(new StandardAnalyzer())
                        .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

                final IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
                writerOpens.incrementAndGet();

                return new DefaultIOContext<IndexWriter>(indexWriter){

                    @Override
                    protected void doClose() throws IOException {
                        super.doClose();
                        writerOpens.decrementAndGet();
                    }
                };

            }
        };

        final IOContext.Provider<IndexSearcher> indexSearcherProvider = new IOContext.Provider<IndexSearcher>() {
            @Override
            public IOContext get() throws IOException {

                final IndexReader indexReader = DirectoryReader.open(directory);
                final IndexSearcher indexSearcher = new IndexSearcher(indexReader);

                readerOpens.incrementAndGet();

                return new AbstractIOContext(indexSearcher) {
                    @Override
                    protected void doClose() throws IOException {
                        readerOpens.decrementAndGet();
                        indexReader.close();
                    }
                };

            }
        };

        return new DefaultObjectIndex(indexWriterProvider, indexSearcherProvider);

    }

    private void createSearchIndex(final Directory directory) throws IOException {

        final IndexWriterConfig indexWriterConfig =
                new IndexWriterConfig(new StandardAnalyzer())
                .setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        try (final IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig)) {
            indexWriter.commit();
        }

    }


}
