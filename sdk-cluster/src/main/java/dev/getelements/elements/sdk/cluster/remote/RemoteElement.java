package dev.getelements.elements.sdk.cluster.remote;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.cluster.remote.proxy.ProxyBuilder;
import dev.getelements.elements.sdk.cluster.remote.record.RemoteElementRecord;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;

/**
 * Represents a remote element.
 */
public interface RemoteElement {

    /**
     * Gets a {@link ServiceLocator} for this {@link RemoteElement} which will return proxy instances of the
     * @return
     */
    ServiceLocator getServiceLocator();

    /**
     * Gets the {@link ElementDefinitionRecord} which provides
     *
     * @return the name of the element.
     */
    RemoteElementRecord getElementRecord();

}
