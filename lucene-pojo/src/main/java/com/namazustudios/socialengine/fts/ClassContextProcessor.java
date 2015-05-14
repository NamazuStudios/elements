package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
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
    public ClassContextProcessor(final Class<?> cls, final IndexableFieldConverter.Provider provider) {

        final SearchableDocument searchableDocument = cls.getAnnotation(SearchableDocument.class);
        final SearchableIdentity searchableIdentity = cls.getAnnotation(SearchableIdentity.class);;

        if (searchableIdentity != null) {
            addSearchableIdentityProcessors(cls, searchableIdentity, provider);
        }

        if (searchableDocument == null) {
            LOG.warn("No @SearchableDocument annotation found on " + cls);
        } else {
            for (final SearchableField searchableField : searchableDocument.fields()) {

                if (SearchableIdentity.CLASS_FIELD_NAME.equals(searchableField.name())) {
                    throw new DocumentGeneratorException(SearchableIdentity.CLASS_FIELD_NAME + " is a reserved name.");
                }

                final FieldMetadata fieldMetadata = new FieldAnnotationFieldMetadata(searchableField);
                addSearchableFieldProcessors(fieldMetadata, cls, provider);

            }
        }

    }

    private void addSearchableIdentityProcessors(final Class<?> cls,
                                                 final SearchableIdentity searchableIdentity,
                                                 final IndexableFieldConverter.Provider provider) {

        contextProcessors.add(new ContextProcessor() {
            @Override
            public void process(JXPathContext context, DocumentEntry<?, ?> documentEntry) {

                final String className = cls.getName();

                final StringField stringField = new StringField(
                        SearchableIdentity.CLASS_FIELD_NAME,
                        className,
                        Field.Store.YES);

                documentEntry.getDocument().removeFields(SearchableIdentity.CLASS_FIELD_NAME);
                documentEntry.getDocument().add(stringField);

            }
        });

        final String className = cls.getName();
        final StringField stringField = new StringField(SearchableIdentity.CLASS_FIELD_NAME, className, Field.Store.YES);

        final FieldMetadata identityAnnotationFieldMetadata = new IdentityAnnotationFieldMetadata(searchableIdentity);
        addSearchableFieldProcessors(identityAnnotationFieldMetadata, cls, provider);

    }

    private void addSearchableFieldProcessors(final FieldMetadata searchableField,
                                              final Class<?> cls,
                                              final IndexableFieldConverter.Provider provider) {


        LOG.debug("Using " + searchableField.converter() + " to process " + searchableField.name() + " on class " + cls);
        final IndexableFieldConverter<Object> indexableFieldConverter = provider.get(searchableField);

        LOG.debug("Compiling JXPath Expression " + searchableField.path() + " for class " + cls);
        final CompiledExpression compiledExpression = JXPathContext.compile(searchableField.path());

        contextProcessors.add(new ContextProcessor() {
            @Override
            public void process(JXPathContext context, DocumentEntry<?,?> documentEntry) {
                final Object value = compiledExpression.getValue(context);
                indexableFieldConverter.process(documentEntry.getDocument(), value, searchableField);
            }
        });

    }

    @Override
    public void process(JXPathContext context, DocumentEntry<?, ?> documentEntry) {

        for (final ContextProcessor contextProcessor : contextProcessors) {
            contextProcessor.process(context, documentEntry);
        }

    }

}
