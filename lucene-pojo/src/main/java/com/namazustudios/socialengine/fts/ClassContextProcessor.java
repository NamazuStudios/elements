package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ContextProcessor} which will process a single annotated class.
 *
 * Created by patricktwohig on 5/13/15.
 */
public class ClassContextProcessor implements ContextProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ClassContextProcessor.class);

    public static final String CLASS_FIELD_NAME = "class";

    private final Class<?> cls;

    private final List<ContextProcessor> contextProcessors = new ArrayList<>();

    /**
     * Creates a new ClassContextProcessor for hte given class and the
     * {@link com.namazustudios.socialengine.fts.IndexableFieldConverter.Provider}
     *
     * @param cls the class
     * @param provider the provider
     */
    public ClassContextProcessor(final Class<?> cls, final IndexableFieldConverter.Provider provider) {

        final SearchableDocument searchableDocument = cls.getAnnotation(SearchableDocument.class);

        if (searchableDocument == null) {
            this.cls = null;
            LOG.warn("No @SearchableDocument annotation found on " + cls);
            return;
        }

        this.cls = cls;

        final SearchableField identity = searchableDocument.identity();

        if (!Field.Store.YES.equals(identity.store())) {
            LOG.warn("Identify for " + cls + " is not stored.");
        }

        addSearchableField(identityFieldMetadata(identity), cls, provider);

        for (final SearchableField searchableField : searchableDocument.fields()) {
            final FieldMetadata fieldMetadata = new AnnotationFieldMetadata(searchableField);
            addSearchableField(fieldMetadata, cls, provider);
        }

    }

    private void addSearchableField(final FieldMetadata searchableField,
                                    final Class<?> cls,
                                    final IndexableFieldConverter.Provider provider) {


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

    private FieldMetadata identityFieldMetadata(final SearchableField searchableField) {
        return new AnnotationFieldMetadata(searchableField) {
            @Override
            public Field.Store store() {
                return Field.Store.YES;
            }
        };
    }

    @Override
    public void process(JXPathContext context, Document document) {

        if (cls != null) {
            final String className = cls.getName();
            final StringField stringField = new StringField(CLASS_FIELD_NAME, className, Field.Store.YES);
        }

        for (final ContextProcessor contextProcessor : contextProcessors) {
            contextProcessor.process(context, document);
        }

    }

}
