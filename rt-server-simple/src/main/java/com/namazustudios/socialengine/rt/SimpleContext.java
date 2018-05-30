package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SimpleContext implements Context {

    private Scheduler scheduler;

    private SchedulerContext schedulerContext;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private HandlerContext handlerContext;

    private AssetLoader assetLoader;

    @Override
    public void start() {
        getResourceContext().start();
        getSchedulerContext().start();
        getIndexContext().start();
        getHandlerContext().start();
    }

    @Override
    public void shutdown() {

        // Stops all contexts first
        getHandlerContext().stop();
        getIndexContext().stop();
        getSchedulerContext().stop();
        getResourceContext().stop();

        // Then stops all services
        getResourceLoader().close();
        getAssetLoader().close();

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

    @Inject
    public void setSchedulerContext(SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
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

    @Inject
    public void setIndexContext(IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    @Inject
    public void setHandlerContext(HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

}
