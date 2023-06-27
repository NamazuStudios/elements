package dev.getelements.elements.rt;

import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.NodeLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static java.lang.Runtime.getRuntime;

@Singleton
public class SimpleContext implements Context, NodeLifecycle {

    private Scheduler scheduler;

    private SchedulerContext schedulerContext;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private HandlerContext handlerContext;

    private TaskContext taskContext;

    private AssetLoader assetLoader;

    private ManifestContext manifestContext;

    private EventContext eventContext;

    private Thread hook = new Thread(this::shutdown);

    private static final Logger logger = LoggerFactory.getLogger(SimpleContext.class);

    @Override
    public void start() {
        getRuntime().addShutdownHook(hook);
        getTaskContext().start();
        getResourceContext().start();
        getSchedulerContext().start();
        getIndexContext().start();
        getHandlerContext().start();
        getManifestContext().start();
    }

    @Override
    public void shutdown() {

        // Remove the shutdown hook.
        if (Thread.currentThread() != hook) getRuntime().removeShutdownHook(hook);

        // Stops all contexts first
        getHandlerContext().stop();
        getIndexContext().stop();
        getSchedulerContext().stop();
        getResourceContext().stop();
        getTaskContext().stop();

        // Then stops all services
        getResourceLoader().close();
        getAssetLoader().close();
        getManifestContext().stop();

    }

    @Override
    public void nodePreStart(Node node) {
        start();
    }

    @Override
    public void nodePostStop(Node node) {
        shutdown();
    }

    @Override
    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Override
    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

    @Override
    public IndexContext getIndexContext() {
        return indexContext;
    }

    @Override
    public HandlerContext getHandlerContext() {
        return handlerContext;
    }

    @Override
    public TaskContext getTaskContext() {
        return taskContext;
    }

    @Override
    public ManifestContext getManifestContext() {
        return manifestContext;
    }

    @Override
    public EventContext getEventContext() {
        return eventContext;
    }

    @Inject
    public void setResourceContext(@Named(LOCAL) ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    @Inject
    public void setSchedulerContext(@Named(LOCAL) SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
    }

    @Inject
    public void setIndexContext(@Named(LOCAL) IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    @Inject
    public void setHandlerContext(@Named(LOCAL) HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    @Inject
    public void setTaskContext(@Named(LOCAL) TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Inject
    public void setManifestContext(@Named(LOCAL) ManifestContext manifestContext) {
        this.manifestContext = manifestContext;
    }

    @Inject
    public void setEventContext(@Named(LOCAL) EventContext eventContext) {
        this.eventContext = eventContext;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    @Inject
    public void setAssetLoader(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

}
