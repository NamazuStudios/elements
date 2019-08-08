package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;

import javax.inject.Inject;

public class ClusterClientContext implements Context {

    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private SchedulerContext schedulerContext;

    private HandlerContext handlerContext;

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

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

    @Override
    public HandlerContext getHandlerContext() {
        return handlerContext;
    }

    @Inject
    public void setHandlerContext(HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

}
