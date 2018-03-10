package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ApplicationNodeContext implements Context {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNodeContext.class);

    private Scheduler scheduler;

    private SchedulerContext schedulerContext;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private HandlerContext handlerContext;

    private AssetLoader assetLoader;

    @Override
    public HandlerContext getHandlerContext() {
        return handlerContext;
    }

    @Override
    public void start() {
        logger.info("Starting context.");
    }

    @Override
    public void shutdown() {
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
