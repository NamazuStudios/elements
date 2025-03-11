package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.InstanceMetadataContext;

import jakarta.inject.Inject;

public class MasterNodeLifecycle implements NodeLifecycle {

    private InstanceMetadataContext instanceMetadataContext;

    @Override
    public void nodePreStart(Node node) {
        getInstanceMetadataContext().start();
    }

    @Override
    public void nodePostStop(Node node) {
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
