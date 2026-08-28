package dev.getelements.elements.sdk.transact;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.path.Path;

public record ResourceListing(Path path, ResourceId resourceId) { }
