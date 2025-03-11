package dev.getelements.elements.dao.mongo.query;

import dev.morphia.Datastore;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.Query;

import jakarta.inject.Inject;
import java.util.Optional;

import static dev.morphia.query.filters.Filters.eq;

public class NameBooleanQueryOperator implements BooleanQueryOperator {

    public static final String PREFIX = ".name.";

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
                final var subquery = getDatastore().find(collectionType);
                subquery.filter(eq("name", input));

                final var referent = subquery.first();
                return Optional.ofNullable(referent);

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
