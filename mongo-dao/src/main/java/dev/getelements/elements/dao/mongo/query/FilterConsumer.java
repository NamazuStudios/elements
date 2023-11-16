package dev.getelements.elements.dao.mongo.query;

import dev.morphia.query.filters.Filter;

@FunctionalInterface
interface FilterConsumer<QueryT> {
    void filter(Filter... filters);
}
