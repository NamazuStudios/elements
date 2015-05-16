package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The abstract implementation of the {@link DocumentGenerator}.  This can accept custom
 * instances of both {@link IndexableFieldExtractor.Provider} and
 * {@link IndexableFieldProcessor.Provider} to process and extract the various encountered fields.
 *
 * This object is considered "heavy" in that one should exist per application.  This object
 * caches instances of {@link ContextProcessor} the first time so that subsequent calls using the
 * same model type will process without requiring reanalysis of the type.
 *
 * This object is thread safe, and uses an instance of {@link ReentrantReadWriteLock} to allow
 * multiple threads to process documents, but only one type may be analyzed at a time.  For maximum
 * performance, it is advised that types are analyzed as part of application bootstrapping.  Howeer,
 * this is not mandatory.
 *
 * Created by patricktwohig on 5/15/15.
 */
public abstract class AbstractDocumentGenerator implements DocumentGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDocumentGenerator.class);

    private static final ContextProcessor EMPTY_CONTEXT_PROCESSOR = new ContextProcessor() {
        @Override
        public void process(JXPathContext context, DocumentEntry<?> documentEntry) {}
    };

    protected final IndexableFieldProcessor.Provider indexableFieldProcessorProvider;
    protected final IndexableFieldExtractor.Provider indexableFieldExtractorProvider;

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock r = reentrantReadWriteLock.readLock();
    private final Lock w = reentrantReadWriteLock.writeLock();

    private final Map<Class<?>, ContextProcessor> contextProcessorMap = new HashMap<>();

    public AbstractDocumentGenerator(final IndexableFieldProcessor.Provider indexableFieldProcessorProvider,
                                     final IndexableFieldExtractor.Provider indexableFieldExtractorProvider) {
        this.indexableFieldProcessorProvider = indexableFieldProcessorProvider;
        this.indexableFieldExtractorProvider = indexableFieldExtractorProvider;
    }

    @Override
    public ContextProcessor analyze(Class<?> cls) {

        Class<?> superclass = cls;
        final List<Class<?>> hierarchy = new ArrayList<>();

        do {
            hierarchy.add(superclass);
            superclass = superclass.getSuperclass();
        } while (superclass != null);

        Collections.reverse(hierarchy);

        w.lock();

        try {
            analyzeHierarchy(hierarchy);
            return contextProcessorMap.get(cls);
        } finally {
            w.unlock();
        }

    }

    private void analyzeHierarchy(final List<Class<?>> classes) {

        for (final Class<?> cls : classes) {

            // If we've already found the context processor for this class
            // we simply skip the addition of the context processor.

            if (contextProcessorMap.containsKey(cls)) {
                continue;
            }

            LOG.debug("Analyzing " + cls + " in DocumentGenerator " + getClass());

            // Finds the superclass context processor, which may or may not exist.
            // it is also possible we're dealing with java.lang.Object

            final ContextProcessor superclassContextProcessor =
                    cls.getSuperclass() == null          ? EMPTY_CONTEXT_PROCESSOR :
                    contextProcessorMap.containsKey(cls) ? contextProcessorMap.get(cls) :
                                                           EMPTY_CONTEXT_PROCESSOR;

            // Checks for the presence of the @SearchableDocument annotation.

            final SearchableDocument searchableDocument = cls.getAnnotation(SearchableDocument.class);

            // Should it be found, we parse out the JXPath expressions and save them
            // for future use (no need to recompile again and again)

            final ContextProcessor classContextProcessor =
                    searchableDocument == null ? EMPTY_CONTEXT_PROCESSOR :
                                                 new ClassContextProcessor(cls, indexableFieldProcessorProvider);

            // Lastly we put in a ContextProcessor that first calls the superclass context processor
            // and then calls the current class' contextProcessor
            contextProcessorMap.put(cls, new ContextProcessor() {

                @Override
                public void process(JXPathContext context, DocumentEntry<?> documentEntry) {
                    superclassContextProcessor.process(context, documentEntry);
                    classContextProcessor.process(context, documentEntry);
                }

            });

        }

    }

    @Override
    public <DocumentT> DocumentEntry<DocumentT> generate(final DocumentT object) {
        return process(object, new Document());
    }

    @Override
    public <DocumentT> DocumentEntry<DocumentT> process(final DocumentT object, final Document document) {

        final JXPathContext jxPathContext = JXPathContext.newContext(object);
        final DocumentEntry documentEntry = new DocumentEntry(document, indexableFieldExtractorProvider);

        final Class<?> cls = object.getClass();
        final ContextProcessor contextProcessor = getOrCreateContextProcessor(cls);

        contextProcessor.process(jxPathContext, documentEntry);

        return documentEntry;

    }

    private ContextProcessor getOrCreateContextProcessor(final Class<?> cls) {

        r.lock();

        try {

            final ContextProcessor out = contextProcessorMap.get(cls);

            if (out != null) {
                return out;
            }

        } finally {
            r.unlock();
        }

        return analyze(cls);

    }

    @Override
    public DocumentEntry<?> entry(final Document document) {
        return new DocumentEntry<Object>(document, indexableFieldExtractorProvider);
    }

    @Override
    public <DocumentT> DocumentEntry<DocumentT> entry(final Class<DocumentT> documentTClass, final Document document) {

        final DocumentEntry<DocumentT> documentEntry = new DocumentEntry<>(document, indexableFieldExtractorProvider);
        final Identity<DocumentT> documentTIdentity = documentEntry.getIdentifier(documentTClass);

        if (!documentTIdentity.getDocumentType().isAssignableFrom(documentTClass)) {
            throw new DocumentException("document type mismatch (" +
                                        documentTClass + " and " + documentTIdentity +
                                        ") are not compatible types.");
        }

        return documentEntry;
    }

    @Override
    public IndexableFieldProcessor.Provider getIndexableFieldProcessorProvider() {
        return indexableFieldProcessorProvider;
    }

    @Override
    public IndexableFieldExtractor.Provider getIndexableFieldExtractorProvider() {
        return indexableFieldExtractorProvider;
    }

}
