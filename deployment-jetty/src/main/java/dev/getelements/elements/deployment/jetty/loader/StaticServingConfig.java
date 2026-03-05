package dev.getelements.elements.deployment.jetty.loader;

import java.util.Map;

/**
 * The fully-resolved serving configuration produced by {@link StaticRuleEngine} for a single static content tree.
 *
 * @param files      file index keyed by forward-slash relative path
 * @param indexFile  metadata for the root index file (e.g. {@code index.html}), or {@code null} if absent
 * @param errorPages map of HTTP status code to the error-page file served for that code
 */
record StaticServingConfig(
        Map<String, StaticFileMetadata> files,
        StaticFileMetadata indexFile,
        Map<Integer, StaticFileMetadata> errorPages) {}