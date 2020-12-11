package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.RemoteInvoker;

import javax.inject.Inject;

public class ClusterClientContext implements Context {

    private RemoteInvoker remoteInvoker;

    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private SchedulerContext schedulerContext;

    private HandlerContext handlerContext;

    private TaskContext taskContext;

    private EventContext eventContext;

    @Override
    public void start() {
        getRemoteInvoker().start();
    }

    @Override
    public void shutdown() {
        getRemoteInvoker().stop();
    }

    public RemoteInvoker getRemoteInvoker() {
        return remoteInvoker;
    }

    @Inject
    public void setRemoteInvoker(RemoteInvoker remoteInvoker) {
        this.remoteInvoker = remoteInvoker;
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

    @Override
    public TaskContext getTaskContext() {
        return taskContext;
    }

    @Inject
    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public EventContext getEventContext() {
        return eventContext;
    }

    @Inject
    public void setEventContext(EventContext eventContext) {
        this.eventContext = eventContext;
    }
}
