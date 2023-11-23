package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.index.BuildIndexRequest;
import dev.getelements.elements.model.index.IndexPlan;

/**
 * Used to access the indexes in the database.
 */
public interface IndexService {

    void build(BuildIndexRequest buildIndexRequest);

    Pagination<IndexPlan<?>> getPlans(int offset, int count);

}
