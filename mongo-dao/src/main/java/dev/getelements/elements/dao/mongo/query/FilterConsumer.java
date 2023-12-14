package dev.getelements.elements.dao.mongo.query;

import dev.morphia.query.filters.Filter;

@FunctionalInterface
interface FilterConsumer {
    void filter(Filter... filters);
}
