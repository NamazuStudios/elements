package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.InstanceMetadataContext;
import dev.getelements.elements.rt.exception.InternalException;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import jakarta.inject.Inject;

/**
 * @deprecated This needs to be replaced with a dispatch to an {@link Element} via {@link ElementRegistry}
 */
@Deprecated
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
