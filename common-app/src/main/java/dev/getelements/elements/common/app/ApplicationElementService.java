package dev.getelements.elements.common.app;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;

import java.util.List;

/**
 * Manages the loaded {@link Element} instances.
 */
public interface ApplicationElementService {

    /**
     * Gets the {@link ElementRegistry} for the {@link Application}, loading it if it has not previously been loaded.
     * @param application the {@link Application}
     * @return the {@link ElementRegistry}
     */
    ElementRegistry getElementRegistry(Application application);

    /**
     * Loads all {@link Element}s defined by the {@link Application}'s code registry. If the {@link Application} has
     * already loaded then this returns the existing collection.
     *
     * @param application the {@link Application}
     * @return an {@link ElementRegistry}
     */
    ApplicationElementRecord getOrLoadApplication(Application application);

    /**
     * A record which holds the registry records for a particular application.
     *
     * @param applicationId the {@link ApplicationId}
     * @param registry the registry for the application
     * @param elements the {@link Element}s associated with the application
     */
    record ApplicationElementRecord(
            ApplicationId applicationId,
            ElementRegistry registry,
            List<Element> elements) {

        public ApplicationElementRecord {
            elements = List.copyOf(elements);
        }

    }

}
