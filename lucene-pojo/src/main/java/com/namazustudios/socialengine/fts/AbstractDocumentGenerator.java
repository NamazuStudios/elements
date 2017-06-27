package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Collections.reverse;

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

    private final IndexableFieldProcessor.Provider indexableFieldProcessorProvider;
    private final IndexableFieldExtractor.Provider indexableFieldExtractorProvider;
    private final JXPathContextProvider jxPathContextProvider;


    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock r = reentrantReadWriteLock.readLock();
    private final Lock w = reentrantReadWriteLock.writeLock();

    private final Map<Class<?>, ContextProcessor> individualClassContextProcessors = new HashMap<>();
    private final Map<Class<?>, ContextProcessor> hierarchicalClassContextProcessors = new HashMap<>();

    public AbstractDocumentGenerator(final IndexableFieldProcessor.Provider indexableFieldProcessorProvider,
                                     final IndexableFieldExtractor.Provider indexableFieldExtractorProvider,
                                     final JXPathContextProvider jxPathContextProvider) {
        this.indexableFieldProcessorProvider = indexableFieldProcessorProvider;
        this.indexableFieldExtractorProvider = indexableFieldExtractorProvider;
        this.jxPathContextProvider = jxPathContextProvider;
    }

    @Override
    public ContextProcessor analyze(Class<?> cls) {

        Class<?> superclass = cls;
        final List<Class<?>> hierarchy = new ArrayList<>();

        do {
            hierarchy.add(superclass);
            superclass = superclass.getSuperclass();
        } while (superclass != null);

        reverse(hierarchy);

        w.lock();

        try {
            analyzeHierarchy(hierarchy);
            return hierarchicalClassContextProcessors.get(cls);
        } finally {
            w.unlock();
        }

    }

    private ContextProcessor analyzeHierarchy(final List<Class<?>> classes) {

        final List<ContextProcessor> contextProcessors = new ArrayList<>();

        classes.stream()
            .filter(c -> c.getAnnotation(SearchableDocument.class) != null)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("@SearchableDocument not found in class hierarchy " + classes));

        // This operates in two phases.  First, it generates the individual
        // class context processors, or fetches them from the already cached
        // list of processors.

        Class<?> theClass = Object.class;
        boolean foundSearchableDocumentAnnotation = false;

        for (final Class<?> cls : classes) {

            LOG.debug("Analyzing " + cls + " in DocumentGenerator " + getClass());

            // Checks for the presence of the @SearchableDocument annotation.

            final SearchableDocument searchableDocument = cls.getAnnotation(SearchableDocument.class);

            if (searchableDocument == null) {
                // We insert an empty context processor in this hierarchy to indicate
                // that there is not an annotation.  When processed, this processor
                // will add nothing to the list of fields to index.
                individualClassContextProcessors.put(cls, EMPTY_CONTEXT_PROCESSOR);
            } else {

                // We analyze the current class in the hierarchy.  If it already exists
                // we pull from the cache.  Otherwise we analyze this the first time.

                foundSearchableDocumentAnnotation = true;

                if (individualClassContextProcessors.containsKey(cls)) {
                    contextProcessors.add(individualClassContextProcessors.get(cls));
                    continue;
                }

                final ContextProcessor contextProcessor = new ClassContextProcessor(cls, indexableFieldProcessorProvider);

                contextProcessors.add(contextProcessor);
                individualClassContextProcessors.put(cls, contextProcessor);

            }

            theClass = cls;

        }

        if (!foundSearchableDocumentAnnotation) {
            throw new IllegalArgumentException("@SearchableDocument not found in class hiearchy " + classes);
        }

        final ContextProcessor contextProcessor;

        // And finally creates a new context processor which simply just runs through the list
        // in order.  Of course, if we didn't find any, we just return the empty context
        // processor.

        if (contextProcessors.isEmpty()) {
            hierarchicalClassContextProcessors.put(theClass, contextProcessor = EMPTY_CONTEXT_PROCESSOR);
        } else {
            hierarchicalClassContextProcessors.put(theClass, contextProcessor = (context, documentEntry) -> {
                for (final ContextProcessor innerProcessor : contextProcessors) {
                    innerProcessor.process(context, documentEntry);
                }
            });
        }

        return contextProcessor;

    }

    @Override
    public <DocumentT> DocumentEntry<DocumentT> generate(final DocumentT object) {
        return process(object, new Document());
    }

    @Override
    public <DocumentT> DocumentEntry<DocumentT> process(final DocumentT object, final Document document) {

        final JXPathContext jxPathContext = jxPathContextProvider.get(object);
        final DocumentEntry documentEntry = entry(document);

        final Class<?> cls = object.getClass();
        final ContextProcessor contextProcessor = getOrCreateContextProcessor(cls);

        contextProcessor.process(jxPathContext, documentEntry);

        return documentEntry;

    }

    private ContextProcessor getOrCreateContextProcessor(final Class<?> cls) {

        r.lock();

        try {

            final ContextProcessor out = hierarchicalClassContextProcessors.get(cls);

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
        return new BasicDocumentEntry<>(document, indexableFieldExtractorProvider);
    }

    @Override
    public <DocumentT> DocumentEntry<DocumentT> entry(final Class<DocumentT> documentTClass, final Document document) {

        final DocumentEntry<DocumentT> documentEntry = new BasicDocumentEntry<>(document, indexableFieldExtractorProvider);
        final Identity<DocumentT> documentTIdentity = documentEntry.getIdentity(documentTClass);

        if (!documentTClass.isAssignableFrom(documentTIdentity.getDocumentType())) {
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
