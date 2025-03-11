package dev.getelements.elements.dao.mongo.query;

import dev.morphia.Datastore;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static dev.morphia.query.filters.Filters.eq;

public class ReferenceBooleanQueryOperator implements BooleanQueryOperator {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceBooleanQueryOperator.class);

    public static final String PREFIX = ".ref.";

    private Mapper mapper;

    private Datastore datastore;

    @Override
    public <QueryT> boolean matches(final Query<QueryT> query, final String fieldSpecification) {
        return fieldSpecification.startsWith(PREFIX);
    }

    @Override
    public <QueryT> Evaluation evaluate(final Query<QueryT> query, final String fieldSpecification) {
        return new Evaluation() {

            final String field = fieldSpecification.substring(PREFIX.length());

            @Override
            public String getField() {
                return field;
            }

            @Override
            public Optional<Object> getValue(final Object input) {

                final var propertyModel = getMapper()
                        .getEntityModel(query.getEntityClass())
                        .getProperty(field);

                if (propertyModel == null) {
                    return Optional.empty();
                }

                final var collectionType = propertyModel.getType();
                final var collectionTypeId = getMapper().findIdProperty(collectionType);

                final Constructor<?> constructor;

                try {
                    constructor = collectionTypeId.getType().getConstructor(input.getClass());
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

                final Object id;

                try {
                    id = constructor.newInstance(input);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    return Optional.empty();
                }

                final var value = getDatastore()
                        .find(collectionType)
                        .filter(eq("_id", id))
                        .first();

                return Optional.ofNullable(value);

            }

        };
    }

    public Mapper getMapper() {
        return mapper;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(final Datastore datastore) {

        if (datastore == null) {
            mapper = null;
        } else {
            this.mapper = datastore.getMapper();
        }

        this.datastore = datastore;

    }

}
