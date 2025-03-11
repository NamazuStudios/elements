package dev.getelements.elements.sdk.service.index;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.index.BuildIndexRequest;
import dev.getelements.elements.sdk.model.index.IndexPlan;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Used to access the indexes in the database.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface IndexService {

    void build(BuildIndexRequest buildIndexRequest);

    Pagination<IndexPlan<?>> getPlans(int offset, int count);

}
