package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.EventContext;
import dev.getelements.elements.rt.HandlerContext;
import dev.getelements.elements.rt.IndexContext;
import dev.getelements.elements.rt.ManifestContext;
import dev.getelements.elements.rt.ResourceContext;
import dev.getelements.elements.rt.SchedulerContext;
import dev.getelements.elements.rt.TaskContext;

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

    private ManifestContext manifestContext;

    private EventContext eventContext;

    @Override
    public void start() {}

    @Override
    public void shutdown() {}

    @Override
    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Inject
    public void setResourceContext(@Named(REMOTE) ResourceContext resourceContext) {
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
    @Override
    public EventContext getEventContext() {
        return eventContext;
    }

    @Inject
    public void setEventContext(@Named(REMOTE) EventContext eventContext) {
        this.eventContext = eventContext;
    }

    @Override
    public ManifestContext getManifestContext() {
        return manifestContext;
    }

    @Inject
    public void setManifestContext(@Named(REMOTE) ManifestContext manifestContext) {
        this.manifestContext = manifestContext;
    }

}
