package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.ResourceAcquisition;
import com.namazustudios.socialengine.rt.ResourceId;
import com.namazustudios.socialengine.rt.ResourceLockService;
import com.namazustudios.socialengine.rt.Scheduler;

import javax.inject.Inject;

public class XodusResourceAcquisition implements ResourceAcquisition {

    private Scheduler scheduler;

    private ResourceLockService resourceLockService;

    private XodusResourceService xodusResourceService;

    @Override
    public void acquire(final ResourceId resourceId) {
        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resourceId)) {
            getXodusResourceService().acquire(resourceId);
        }
    }

    @Override
    public void scheduleRelease(final ResourceId resourceId) {
        getScheduler().submitV(() -> {
            try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resourceId)) {
                getXodusResourceService().release(resourceId);
            }
        });
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ResourceLockService getResourceLockService() {
        return resourceLockService;
    }

    @Inject
    public void setResourceLockService(ResourceLockService resourceLockService) {
        this.resourceLockService = resourceLockService;
    }

    public XodusResourceService getXodusResourceService() {
        return xodusResourceService;
    }

    @Inject
    public void setXodusResourceService(XodusResourceService xodusResourceService) {
        this.xodusResourceService = xodusResourceService;
    }

}
