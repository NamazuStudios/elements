package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.record.ElementRecord;

import java.net.URI;
import java.util.List;
import java.util.Set;

public record ApplicationDeployment(
        String status,
        Set<URI> uris,
        List<String> logs,
        String applicationId,
        List<ElementRecord> elements
) {}
