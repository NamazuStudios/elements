package dev.getelements.elements.dao.mongo.query;

import dev.morphia.query.Query;

import java.util.Optional;

public interface BooleanQueryOperator {

    <QueryT> boolean matches(Query<QueryT> query, String fieldSpecification);

    <QueryT> Evaluation evaluate(Query<QueryT> query, String fieldSpecification);

    interface Evaluation {

        String getField();

        Optional<Object> getValue(Object input);

    }

    BooleanQueryOperator DEFAULT = new BooleanQueryOperator() {

        @Override
        public <QueryT> boolean matches(final Query<QueryT> query, final String field) {
            return true;
        }

        @Override
        public <QueryT> Evaluation evaluate(final Query<QueryT> query, final String field) {
            return new Evaluation() {
                @Override
                public String getField() {
                    return field;
                }

                @Override
                public Optional<Object> getValue(final Object input) {
                    return Optional.of(input);
                }
            };
        }

    };

}
