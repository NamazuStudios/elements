package com.namazustudios.socialengine.rt;

import javax.inject.Inject;

public class SimpleResourceContext implements ResourceContext {

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    @Override
    public ResourceId create(final Path path, final String module, final Object ... args) {
        final Resource resource = getResourceLoader().load(module, args);
        getResourceService().addResource(path, resource);
        return resource.getId();
    }

    @Override
    public void destroy(final ResourceId resourceId) {
        getResourceService().destroy(resourceId);
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

}
