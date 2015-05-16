package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

/**
 * This creates and manages instances of {@link ContextProcessor}s which can be usd
 * to genrerate instances of {@link DocumentEntry}.
 *
 * Created by patricktwohig on 5/12/15.
 */
public interface DocumentGenerator {

    /**
     * Analyzes the given {@link Class}, searching for the presence of the {@link SearchableDocument}
     * annotations generating an index of the fields.  This walks the whole hierarchy until it
     * hits {@link Object} generating a {@link ContextProcessor} which can be used to add
     * {@link IndexableField} instances to a {@link DocumentEntry}.
     *
     * @param cls
     * @return a {@link ContextProcessor} which can be used to proces a context and Document
     */
    ContextProcessor analyze(final Class<?> cls);

    /**
     * Generates a {@link Document} from the given object.
     *
     * @param object the object to index, not null
     * @return a Document which can be written to the search index.
     *
     */
    <DocumentT> DocumentEntry<DocumentT> generate(final DocumentT object);

    /***
     * Processes the given object, adding all found {@link org.apache.lucene.index.IndexableField}
     * instances to it.
     *
     * @param object the Object to index, not null
     * @param document the target document
     *
     */
    <DocumentT> DocumentEntry<DocumentT> process(final DocumentT object, final Document document);

    /**
     * Creates a new {@link DocumentEntry} from the given {@link Document}.  This returns
     * an empty entry with no
     *
     * @param document
     * @return
     */
    DocumentEntry<?> entry(final Document document);

    /**
     * Creates a new {@link DocumentEntry} from the given {@link Document}.
     *
     * @param document
     * @return
     */
    <DocumentT> DocumentEntry<DocumentT> entry(final Class<DocumentT> documentTClass, final Document document);

    /**
     * Gets the {@link IndexableFieldExtractor.Provider} used to extra field data rom
     * {@link Document instances.}
     *
     * @return
     */
    IndexableFieldProcessor.Provider getIndexableFieldProcessorProvider();

    /**
     * Gets the {@link IndexableFieldExtractor.Provider} used to extra field data rom
     * {@link Document instances.}
     *
     * @return the extractor
     */
    IndexableFieldExtractor.Provider getIndexableFieldExtractorProvider();

}
