package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.sun.org.apache.regexp.internal.RE;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A type of {@link Context} which is used to house remotely invokable instances.
 */
public class ClusterContext implements Context {

    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private SchedulerContext schedulerContext;

    private HandlerContext handlerContext;

    private TaskContext taskContext;

    @Override
    public void start() {}

    @Override
    public void shutdown() {}

    @Override
    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Inject
    public void setResourceContext(@Named(REMOTE)ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    @Override
    public IndexContext getIndexContext() {
        return indexContext;
    }

    @Inject
    public void setIndexContext(@Named(REMOTE) IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    @Override
    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

    @Inject
    public void setSchedulerContext(@Named(REMOTE) SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
    }

    @Override
    public HandlerContext getHandlerContext() {
        return handlerContext;
    }

    @Inject
    public void setHandlerContext(@Named(REMOTE) HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    @Override
    public TaskContext getTaskContext() {
        return taskContext;
    }

    @Inject
    public void setTaskContext(@Named(REMOTE) TaskContext taskContext) {
        this.taskContext = taskContext;
    }

}
