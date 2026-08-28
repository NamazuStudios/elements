package dev.getelements.elements.sdk.transact;

import dev.getelements.elements.sdk.cluster.id.ResourceId;

public record UnlinkResource(ResourceId resourceId, boolean removed) { }
