package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.record.ElementRecord;

import java.nio.file.Path;

/**
 * Bundles an {@link ElementRecord} with the on-disk {@link Path} and source package coordinate for an element
 * that failed to load. {@code sourceElmArtifact} is the Maven coordinates of the package ELM that contributed
 * this element, or {@code null} if the element came from the deployment's own ELM file or an element definition.
 */
record FailedElementEntry(ElementRecord record, Path elementPath, String sourceElmArtifact) {}
