package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.system.ElementMetadata;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Indicates the status of an application deployment.
 *
 * @param status the status
 * @param uris the uris serving
 * @param logs the logs of the deployment
 * @param elements the elements associated with the deployment
 */
public record ApplicationStatus(
        Application application,
        String status,
        Set<URI> uris,
        List<String> logs,
        List<ElementMetadata> elements
) {}
