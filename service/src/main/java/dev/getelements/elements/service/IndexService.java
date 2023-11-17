package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.index.BuildIndexRequest;
import dev.getelements.elements.model.index.IndexPlan;

public interface IndexService {

    void build(BuildIndexRequest buildIndexRequest);

    Pagination<IndexPlan<?>> getPlans(int offset, int count);

}
