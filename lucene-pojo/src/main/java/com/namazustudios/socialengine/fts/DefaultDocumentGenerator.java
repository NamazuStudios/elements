package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
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
        public void process(JXPathContext context, Document document) {}
    };

    private static final IndexableFieldConverter.Provider DEFAULT_CONVERTER_PROVIDER =
            new IndexableFieldConverter.Provider() {
                @Override
                public <T> IndexableFieldConverter<T> get(SearchableField searchableField) {
                    try {
                        return searchableField.converter().newInstance();
                    } catch (IllegalAccessException ex) {
                        throw new DocumentGeneratorException(ex);
                    } catch (InstantiationException ex) {
                        throw new DocumentGeneratorException(ex);
                    }
                }
            };

    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock r = reentrantReadWriteLock.readLock();
    private final Lock w = reentrantReadWriteLock.writeLock();

    private final IndexableFieldConverter.Provider provider;
    private final Map<Class<?>, ContextProcessor> contextProcessorMap = new HashMap<>();

    public DefaultDocumentGenerator() {
        this(DEFAULT_CONVERTER_PROVIDER);
    }

    public DefaultDocumentGenerator(IndexableFieldConverter.Provider provider) {
        this.provider = provider;
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
                                                 new ClassContextProcessor(cls, null);

            // Lastly we put in a ContextProcessor that first calls the superclass context processor
            // and then calls the current class' contextProcessor
            contextProcessorMap.put(cls, new ContextProcessor() {

                @Override
                public void process(JXPathContext context, Document document) {
                    superclassContextProcessor.process(context, document);
                    classContextProcessor.process(context, document);
                }

            });

        }

    }

    @Override
    public Document generate(Object object) {
        final Document document = new Document();
        process(object, document);
        return document;
    }

    @Override
    public void process(Object object, Document document) {

        final JXPathContext jxPathContext = JXPathContext.newContext(object);

        final Class<?> cls = object.getClass();
        final ContextProcessor contextProcessor = getOrCreateContextProcessor(cls);

        contextProcessor.process(jxPathContext, document);

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
