package com.namazustudios.socialengine.fts;

import org.apache.lucene.document.Document;

import java.util.*;

/**
 * Created by patricktwohig on 6/1/15.
 */
public class TypeFieldExtractor implements IndexableFieldExtractor<Object> {

    @Override
    public Object extract(Document document, FieldMetadata fieldMetadata) {
        if (Class.class.isAssignableFrom(fieldMetadata.type())) {
            return extractClass(document, fieldMetadata);
        } else {
            throw new FieldExtractionException(fieldMetadata, document, "unable to extract value for field "
                    + fieldMetadata);
        }
    }

    private Class<?> extractClass(final Document document, final FieldMetadata fieldMetadata) {

        // Load the classes and filter out any interface or annotation types, which
        // shouldn't be in here but we can't trust that the incoming document.

        final List<Class<?>> classList = new ArrayList<>();

        for (final String className : document.getValues(fieldMetadata.name())) {
            try {

                final Class<?> cls = Class.forName(className);

                // We really only care about concrete types here, so if it's
                // an interface we skip over it.  Trying to support interfaces
                // (or multiple inheritance) would be a nightmare.

                if (!cls.isInterface() && !cls.isAnnotation()) {
                    classList.add(cls);
                }

            } catch (ClassNotFoundException ex) {
                throw new FieldExtractionException(fieldMetadata, document, ex);
            }
        }

        final SortedSet<Class<?>> classSortedSet = new TreeSet<>(new Comparator<Class<?>>() {

            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return  o1.equals(o2)            ?  0 :
                        o1.isAssignableFrom(o2)  ? -1 : 1;
            }

        });

        classSortedSet.addAll(classList);

        if (classSortedSet.isEmpty()) {
            throw new FieldExtractionException(fieldMetadata, document);
        }

        // Do a sanity check to ensure that each type we've found is actually
        // part of the same hierarchy.

        Iterator<Class<?>> itr = classSortedSet.iterator();
        Class<?> superclass = itr.next();

        while (itr.hasNext()) {

            final Class<?> cls = itr.next();

            if (!superclass.isAssignableFrom(cls)) {
                throw new FieldExtractionException(fieldMetadata, document,
                        "Unable to understand the class heirarchy for document.  Found unrelated type " + cls);
            }

            superclass = cls;

        }

        // Finally, returns the most specific subclass in the hierarchy.

        return classSortedSet.last();

    }

}
