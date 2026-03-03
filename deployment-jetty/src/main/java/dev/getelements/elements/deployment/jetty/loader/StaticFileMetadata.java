package dev.getelements.elements.deployment.jetty.loader;

import java.nio.file.Path;
import java.util.Map;

/**
 * Pre-computed metadata for a single static file. All header values are fully resolved at load time so that
 * {@link StaticContentServlet} can serve them without any per-request processing.
 *
 * @param absolutePath the absolute path to the file on disk
 * @param mimeType the resolved MIME type (never {@code null}; falls back to {@code application/octet-stream})
 * @param resolvedHeaders resolved response headers to set on every response for this file
 */
record StaticFileMetadata(Path absolutePath, String mimeType, Map<String, String> resolvedHeaders) {}
