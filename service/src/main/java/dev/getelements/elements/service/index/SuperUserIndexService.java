package dev.getelements.elements.service.index;

import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.index.BuildIndexRequest;
import dev.getelements.elements.model.index.IndexPlan;
import dev.getelements.elements.service.IndexService;

import javax.inject.Inject;

public class SuperUserIndexService implements IndexService {

    private IndexDao indexDao;

    @Override
    public void build(final BuildIndexRequest buildIndexRequest) {

        if (buildIndexRequest.isPlan()) {
            getIndexDao().planAll();
        }

        if (buildIndexRequest.isBuildCustom()) {
            try (var indexer = getIndexDao().beginIndexing()) {
                indexer.buildAllCustom();
            }
        }

    }

    @Override
    public Pagination<IndexPlan<?>> getPlans(int offset, int count) {
        return getIndexDao().getPlans(offset, count);
    }

    public IndexDao getIndexDao() {
        return indexDao;
    }

    @Inject
    public void setIndexDao(IndexDao indexDao) {
        this.indexDao = indexDao;
    }

}
