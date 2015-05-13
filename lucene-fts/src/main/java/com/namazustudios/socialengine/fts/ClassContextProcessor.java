package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricktwohig on 5/13/15.
 */
public class ClassContextProcessor implements ContextProcessor {

    private final List<ContextProcessor> contextProcessors = new ArrayList<>();

    public ClassContextProcessor(Class<?> cls) {

        final SearchableDocument searchableDocument = cls.getAnnotation(SearchableDocument.class);

        for (final SearchableField searchableField : searchableDocument.value()) {

            final IndexableFieldConverter<Object> indexableFieldConverter;

            try {
                indexableFieldConverter = searchableField.converter().newInstance();
            } catch (IllegalAccessException ex) {
                throw new IndexException(ex);
            } catch (InstantiationException ex) {
                throw new IndexException(ex);
            }

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
