package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by patricktwohig on 6/1/15.
 */
public class TypeFieldProcessor extends AbstractIndexableFieldProcessor<Object> {

    @Override
    public void process(Document document, Object value, FieldMetadata fieldMetadata) {

        if (value instanceof Class<?>) {

            document.removeFields(fieldMetadata.name());

            final List<Class<?>> hierarchy = new ArrayList<>();

            Class<?> supertype = (Class<?>)value;

            do {
                hierarchy.add(supertype);
                supertype = supertype.getSuperclass();
            } while (supertype != null);

            Collections.reverse(hierarchy);

            for (final Class<?> cls : hierarchy) {
                document.add(newStringField(cls.getName(), fieldMetadata));
            }

        } else {
            throw new DocumentGenerationException(document, value, fieldMetadata,
                    "value (" + value + ") does not specify and instance of " + Class.class + "  for " + fieldMetadata);
        }

    }

}
