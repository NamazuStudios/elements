package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class SimpleContext implements Context {

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private AssetLoader rootAssetLoader;

    @Override
    public void start() {}

    @Override
    public void shutdown() {
        getScheduler().shutdown();
        getResourceService().removeAndCloseAllResources();
        getResourceLoader().close();
        getRootAssetLoader().close();
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

    public AssetLoader getRootAssetLoader() {
        return rootAssetLoader;
    }

    @Inject
    public void setRootAssetLoader(AssetLoader rootAssetLoader) {
        this.rootAssetLoader = rootAssetLoader;
    }

}
