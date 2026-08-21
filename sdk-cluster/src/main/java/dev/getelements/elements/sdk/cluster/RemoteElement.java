package dev.getelements.elements.sdk.cluster;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;

/**
 * Represents a remote element.
 */
public interface RemoteElement {

    /**
     * Gets the {@link ElementDefinitionRecord} which provides
     *
     * @return the name of the element.
     */
    ElementRecord getElementRecord();

    /**
     * Gets the {@link ServiceLocator} associated with this element.
     *
     * @return the {@link ServiceLocator}
     */
    ServiceLocator getServiceLocator();

}
