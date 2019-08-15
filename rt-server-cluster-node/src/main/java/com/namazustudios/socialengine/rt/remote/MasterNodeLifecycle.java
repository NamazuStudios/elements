package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.InstanceMetadataContext;

import javax.inject.Inject;

public class MasterNodeLifecycle implements NodeLifecycle {

    private InstanceMetadataContext instanceMetadataContext;

    @Override
    public void preStart() {
        getInstanceMetadataContext().start();
    }

    @Override
    public void postStop() {
        getInstanceMetadataContext().stop();
    }

    public InstanceMetadataContext getInstanceMetadataContext() {
        return instanceMetadataContext;
    }

    @Inject
    public void setInstanceMetadataContext(InstanceMetadataContext instanceMetadataContext) {
        this.instanceMetadataContext = instanceMetadataContext;
    }

}
