package com.namazustudios.socialengine.rt;

import javax.inject.Inject;

public class ClusterClientContext implements Context {

    // TODO Implement this class (Skeleton Only At the Moment)


    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private SchedulerContext schedulerContext;

    @Override
    public void start() {
        // TODO: Connect to the service
    }

    @Override
    public void shutdown() {
        // TODO: Disconnect from the Service
    }

    @Override
    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Inject
    public void setResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    @Override
    public IndexContext getIndexContext() {
        return indexContext;
    }

    @Inject
    public void setIndexContext(IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    @Override
    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

    @Inject
    public void setSchedulerContext(SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
    }

}
