package dev.getelements.elements.dao.mongo.query;

import dev.morphia.Datastore;
import dev.morphia.mapping.Mapper;
import dev.morphia.query.Query;

import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

import static dev.morphia.query.filters.Filters.*;

public class ForwardNameBooleanQueryOperator implements BooleanQueryOperator {

    private Mapper mapper;

    private Datastore datastore;

    @Override
    public <QueryT> boolean matches(final Query<QueryT> query, final String fieldSpecification) {
        return fieldSpecification.equals("name") || fieldSpecification.endsWith(".name");
    }

    @Override
    public <QueryT> Evaluation evaluate(final Query<QueryT> query, final String fieldSpecification) {
        return new Evaluation() {

            // Remove a single trailing ".name" or "name"
            // e.g., "projects.name" -> "projects", "name" -> ""
            final String containerField = fieldSpecification.replaceFirst("\\.?name$", "");

            @Override
            public String getField() {

                if (containerField.isEmpty()) {
                    final var rootModel = getMapper().getEntityModel(query.getEntityClass());
                    final var nameProp = rootModel.getProperty("name");
                    return nameProp != null ? nameProp.getMappedName() : "name";
                }

                return containerField;
            }

            @Override
            public Optional<Object> getValue(final Object input) {

                final var entityModel = getMapper().getEntityModel(query.getEntityClass());

                // containerField is the field BEFORE ".name" ("" if top-level)
                final String containerField = fieldSpecification.replaceFirst("\\.?name$", "");

                // Compile "contains" regex once (case-insensitive)
                final var pattern = Pattern.compile(
                        Pattern.quote(input.toString()), Pattern.CASE_INSENSITIVE);

                if (containerField.isEmpty()) {
                    // Root-level name: return a Filter to apply directly to base query.
                    final var rootNameProp = entityModel.getProperty("name");

                    if (rootNameProp == null) {
                        return Optional.empty();
                    }

                    final var storedRootName = rootNameProp.getMappedName();

                    return Optional.of(regex(storedRootName, pattern)); // <— Filter
                }

                // Nested case: resolve the property and element type
                final var propertyModel = entityModel.getProperty(containerField);

                if (propertyModel == null) {
                    return Optional.empty();
                }

                final var typeData = propertyModel.getTypeData();
                final Class<?> elementType = typeData.getTypeParameters().isEmpty()
                        ? typeData.getType()
                        : typeData.getTypeParameters().getFirst().getType();

                // Does the element have a "name" field?
                final var elementModel = getMapper().getEntityModel(elementType);
                final var nameProp = elementModel.getProperty("name");

                if (nameProp == null) {
                    return Optional.empty();
                }

                // If this is a reference property:
                if (propertyModel.isReference()) {

                    // Find ALL matching referents by name (contains)
                    try(final var matchIterator = getDatastore()
                            .find(elementType)
                            .filter(regex(nameProp.getMappedName(), pattern))
                            .iterator()) {

                        final var matches = matchIterator.toList();
                        return matches.isEmpty() ? Optional.empty() : Optional.of(matches);
                    }

                }

                // If the property is a collection or array
                final var isCollectionLike =
                        Collection.class.isAssignableFrom(propertyModel.getType()) ||
                                propertyModel.getType().isArray();

                // Embedded cases
                if (isCollectionLike) {
                    // array of embedded docs -> elemMatch on the container field
                    // NOTE: elemMatch expects a filter on the element's fields:
                    return Optional.of(elemMatch(containerField, regex("name", pattern))); // <— Filter
                } else {
                    // single embedded doc -> regex on "<container>.name"
                    return Optional.of(regex(containerField + ".name", pattern)); // <— Filter
                }
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
