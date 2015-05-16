package com.namazustudios.socialengine.fts;

import com.namazustudios.socialengine.fts.annotation.SearchableField;
import com.namazustudios.socialengine.fts.annotation.SearchableIdentity;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.lucene.search.Query;

/**
 * Created by patricktwohig on 5/16/15.
 */
public class ExampleObjectQuery<DocumentT> extends IdentityObjectQuery<DocumentT> {


    private static Object extractIdentifierFromInstance(Object documentTInstance) {

        Class<?> cls = documentTInstance.getClass();
        SearchableField searchableField = null;

        do {

            final SearchableIdentity searchableIdentity = cls.getAnnotation(SearchableIdentity.class);

            if (searchableIdentity != null) {
                searchableField = searchableIdentity.value();
                break;
            }

            cls = cls.getSuperclass();

        } while (cls != null);

        if (searchableField == null) {
            throw new DocumentException("cannot find " +
                    SearchableIdentity.class +
                    " anywhere in the type" +
                    " heirarchy for " +
                    cls);
        }

        final JXPathContext jxPathContext = JXPathContext.newContext(documentTInstance);
        return jxPathContext.getValue(searchableField.path());

    }

    public ExampleObjectQuery(Class<DocumentT> documentType,
                              IndexableFieldProcessor.Provider indexableFieldProcessorProvider,
                              DocumentT documentTInstance) {
        super(documentType, indexableFieldProcessorProvider, extractIdentifierFromInstance(documentTInstance));
    }

}

