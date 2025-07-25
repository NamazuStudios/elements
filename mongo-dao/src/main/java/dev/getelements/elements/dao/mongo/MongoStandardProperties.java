package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.rt.exception.InternalException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class MongoStandardProperties {

    public static final String NAME_PROPERTY = "name";
    public static final String METADATA_PROPERTY = "metadata";
    public static final String METADATA_SPEC_PROPERTY = "metadataSpec";

    private MongoStandardProperties() {}


    static <ModelT> Function<ModelT, String> getNameExtractor(final Class<ModelT> model) {
        try {

            for (final var pd : Introspector.getBeanInfo(model).getPropertyDescriptors()) {
                if (NAME_PROPERTY.equals(pd.getName()) && String.class.isAssignableFrom(pd.getPropertyType())) {

                    return obj -> {
                        try {
                            return (String) pd.getReadMethod().invoke(obj);
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            throw new InternalException(ex);
                        }
                    };

                }
            }

            throw new IllegalArgumentException("No name found for " + model.getName());

        } catch (IntrospectionException e) {
            throw new InternalException("Failed to introspect model " + model, e);
        }

    }

    static <ModelT> Function<ModelT, MongoMetadataSpec> getMetadataSpecExtractor(final Class<ModelT> model) {
        try {

            for (final var pd : Introspector.getBeanInfo(model).getPropertyDescriptors()) {
                if (METADATA_SPEC_PROPERTY.equals(pd.getName()) &&
                    MongoMetadataSpec.class.isAssignableFrom(pd.getPropertyType())) {

                    return obj -> {
                        try {
                            return (MongoMetadataSpec) pd.getReadMethod().invoke(obj);
                        } catch (IllegalAccessException | InvocationTargetException ex) {
                            throw new InternalException(ex);
                        }
                    };

                }
            }

            throw new IllegalArgumentException("No metadata spec found for " + model.getName());

        } catch (IntrospectionException e) {
            throw new InternalException("Failed to introspect model " + model, e);
        }

    }
}
