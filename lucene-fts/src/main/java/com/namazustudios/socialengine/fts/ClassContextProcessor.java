package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ContextProcessor} which will process a single annotated class.
 *
 * Created by patricktwohig on 5/13/15.
 */
public class ClassContextProcessor implements ContextProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ClassContextProcessor.class);

    private final List<ContextProcessor> contextProcessors = new ArrayList<>();

    /**
     * Creates a new ClassContextProcessor for hte given class and the
     * {@link com.namazustudios.socialengine.fts.IndexableFieldConverter.Provider}
     *
     * @param cls the class
     * @param provider the provider
     */
    public ClassContextProcessor(Class<?> cls, IndexableFieldConverter.Provider provider) {

        final SearchableDocument searchableDocument = cls.getAnnotation(SearchableDocument.class);

        if (searchableDocument == null) {
            LOG.warn("No @SearchableDocument annotation found on " + cls);
            return;
        }

        for (final SearchableField searchableField : searchableDocument.value()) {

            LOG.debug("Using " + searchableField.converter() + " to process " + searchableField.name() + " on class " + cls);
            final IndexableFieldConverter<Object> indexableFieldConverter = provider.get(searchableField);

            LOG.debug("Compiling JXPath Expression " + searchableField.path() + " for class " + cls);
            final CompiledExpression compiledExpression = JXPathContext.compile(searchableField.path());

            contextProcessors.add(new ContextProcessor() {
                @Override
                public void process(JXPathContext context, Document document) {
                    final Object value = compiledExpression.getValue(context);
                    indexableFieldConverter.process(document, value, searchableField);
                }
            });

        }

    }

    @Override
    public void process(JXPathContext context, Document document) {
        for (final ContextProcessor contextProcessor : contextProcessors) {
            contextProcessor.process(context, document);
        }
    }

}
