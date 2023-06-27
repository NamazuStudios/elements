package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.InstanceMetadataContext;
import dev.getelements.elements.rt.exception.InternalException;

import javax.inject.Inject;

public class MasterNodeLocalInvocationDispatcher extends AbstractLocalInvocationDispatcher {

    private InstanceMetadataContext instanceMetadataContext;

    @Override
    protected Object resolve(final Class<?> type) {
        if (InstanceMetadataContext.class.equals(type)) {
            return getInstanceMetadataContext();
        } else {
            throw new InternalException("No dispatch-mapping for type: " + type);
        }
    }

    public InstanceMetadataContext getInstanceMetadataContext() {
        return instanceMetadataContext;
    }

    @Inject
    public void setInstanceMetadataContext(InstanceMetadataContext instanceMetadataContext) {
        this.instanceMetadataContext = instanceMetadataContext;
    }

}
