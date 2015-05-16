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
 * The default instance of the {@link DocumentGenerator}, this is a simple
 * implementation that just reads the annotations and converts.
 *
 * Created by patricktwohig on 5/12/15.
 */
public class DefaultDocumentGenerator implements DocumentGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDocumentGenerator.class);

    private static final ContextProcessor EMPTY_CONTEXT_PROCESSOR = new ContextProcessor() {
        @Override
        public void process(JXPathContext context, DocumentEntry<?,?> documentEntry) {}
    };

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    private final Lock r = reentrantReadWriteLock.readLock();
    private final Lock w = reentrantReadWriteLock.writeLock();

    private final IndexableFieldProcessor.Provider indexableFieldProcessorProvider;
    private final IndexableFieldExtractor.Provider indexableFieldExtractorProvider;

    private final Map<Class<?>, ContextProcessor> contextProcessorMap = new HashMap<>();

    public DefaultDocumentGenerator() {
        this(DefaultIndexableFieldProcessorProvider.getInstance(),
             DefaultIndexableFieldExtractorProvider.getInstance());
    }

    public DefaultDocumentGenerator(
            final IndexableFieldProcessor.Provider indexableFieldProcessorProvider,
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
                public void process(JXPathContext context, DocumentEntry<?,?> documentEntry) {
                    superclassContextProcessor.process(context, documentEntry);
                    classContextProcessor.process(context, documentEntry);
                }

            });

        }

    }

    @Override
    public <DocumentT> DocumentEntry<? extends DocumentT, ?> generate(final DocumentT object) {
        return process(object, new Document());
    }

    @Override
    public <DocumentT> DocumentEntry<? extends DocumentT, ?> process(final DocumentT object, final Document document) {

        final JXPathContext jxPathContext = JXPathContext.newContext(object);
        final DocumentEntry generatorDocumentEntry = new DocumentEntry(document, indexableFieldExtractorProvider);

        final Class<?> cls = object.getClass();
        final ContextProcessor contextProcessor = getOrCreateContextProcessor(cls);

        contextProcessor.process(jxPathContext, generatorDocumentEntry);

        return null;

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

        analyze(cls);
        return getOrCreateContextProcessor(cls);

    }

}
