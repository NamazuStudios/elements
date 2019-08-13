package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.exception.InternalException;

import javax.inject.Inject;

public class MasterNodeInvocationDispatcher extends AbstractInvocationDispatcher {

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
