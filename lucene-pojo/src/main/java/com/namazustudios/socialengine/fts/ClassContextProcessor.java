package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableDocument;
import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.document.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ContextProcessor} which will process a single annotated class.
 * This does not touch any other types in the hierarchy.
 *
 * This will skip the class completely if {@link SearchableDocument} is missing,
 * as it contains information necessary to properly index the object.
 *
 * It is important to node that, for the sake of efficiency, this pre-compiles
 * the XPath expressions and saves them in memory.
 *
 * This class is thread safe as it is immutable.
 *
 * Created by patricktwohig on 5/13/15.
 */
public class ClassContextProcessor implements ContextProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ClassContextProcessor.class);

    private final List<ContextProcessor> contextProcessors = new ArrayList<>();

    /**
     * Creates a new ClassContextProcessor for hte given class and the
     * {@link IndexableFieldProcessor.Provider}
     *
     * @param cls the class
     * @param provider the provider
     */
    public ClassContextProcessor(final Class<?> cls, final IndexableFieldProcessor.Provider provider) {

        final SearchableDocument searchableDocument = cls.getAnnotation(SearchableDocument.class);
        final SearchableIdentity searchableIdentity = cls.getAnnotation(SearchableIdentity.class);;

        if (searchableDocument == null) {
            LOG.warn("No @SearchableDocument annotation found on " + cls);
        } else {

            addSearchableTypeProcessor(cls, searchableDocument, provider);

            if (searchableIdentity != null) {
                addSearchableIdentityProcessors(cls, searchableIdentity, provider);
            }

            for (final SearchableField searchableField : searchableDocument.fields()) {
                final FieldMetadata fieldMetadata = new AnnotationFieldMetadata(searchableField);
                addSearchableFieldProcessors(fieldMetadata, cls, provider);
            }

        }

    }

    private void addSearchableTypeProcessor(final Class<?> cls,
                                            final SearchableDocument searchableDocument,
                                            final IndexableFieldProcessor.Provider provider) {

        final FieldMetadata typeFieldMetadata = new AnnotationFieldMetadata(searchableDocument.type()) {

            @Override
            public Field.Store store() {
                return Field.Store.YES;
            }

        };

        addSearchableFieldProcessors(typeFieldMetadata, cls, provider);

    }

    private void addSearchableIdentityProcessors(final Class<?> cls,
                                                 final SearchableIdentity searchableIdentity,
                                                 final IndexableFieldProcessor.Provider provider) {

        final FieldMetadata identityFieldMetadata = new AnnotationFieldMetadata(searchableIdentity.value()) {

            @Override
            public Field.Store store() {
                return Field.Store.YES;
            }

        };

        addSearchableFieldProcessors(identityFieldMetadata, cls, provider);

    }

    private void addSearchableFieldProcessors(final FieldMetadata searchableField,
                                             final Class<?> cls,
                                             final IndexableFieldProcessor.Provider provider) {

        for (final Class<? extends  IndexableFieldProcessor> implementationClass : searchableField.processors()) {

            LOG.debug("Using " + implementationClass + " to process " + searchableField.name() + " on class " + cls);
            final IndexableFieldProcessor<Object> indexableFieldProcessor = provider.get(searchableField, implementationClass);

            LOG.debug("Compiling JXPath Expression " + searchableField.path() + " for class " + cls);
            final CompiledExpression compiledExpression = JXPathContext.compile(searchableField.path());

            contextProcessors.add(new ContextProcessor() {
                @Override
                public void process(JXPathContext context, DocumentEntry<?> documentEntry) {
                    final Object value = compiledExpression.getValue(context);
                    indexableFieldProcessor.process(documentEntry.getDocument(), value, searchableField);
                }
            });

        }


    }

    @Override
    public void process(JXPathContext context, DocumentEntry<?> documentEntry) {

        for (final ContextProcessor contextProcessor : contextProcessors) {
            contextProcessor.process(context, documentEntry);
        }

    }

}
