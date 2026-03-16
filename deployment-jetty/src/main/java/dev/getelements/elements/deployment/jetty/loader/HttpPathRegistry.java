package dev.getelements.elements.deployment.jetty.loader;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry of HTTP path prefixes that are owned by the server or by deployed elements.
 *
 * <p>Loaders register their context paths on load and deregister on unload. System API paths
 * (e.g. {@code /api/rest}, {@code /cdn/git}) are pre-seeded at startup by the server module.
 * {@link StaticContentLoader} queries this registry to detect path conflicts before deploying
 * a static content handler.</p>
 */
public class HttpPathRegistry {

    /**
     * Describes the relationship between a proposed context path and the currently registered paths.
     */
    public enum PathConflict {
        /** No conflict — deploy the handler as-is. */
        NONE,
        /**
         * The proposed path is inside a registered namespace (e.g. proposing {@code /app/rest/foo}
         * when {@code /app/rest} is registered). Static content must not be deployed here.
         */
        INNER,
        /**
         * The proposed path is a parent of one or more registered paths (e.g. proposing {@code /}
         * when {@code /api/rest} is registered). Static content may be deployed as a catch-all but
         * must skip registered paths at request time.
         */
        OUTER
    }

    private final Set<String> registeredPrefixes = ConcurrentHashMap.newKeySet();

    /**
     * Registers a path prefix as owned. Safe to call concurrently.
     *
     * @param pathPrefix the raw path prefix (without {@code HTTP_PATH_PREFIX})
     * @return {@code true} if the prefix was newly registered; {@code false} if it was already registered
     *         (indicating a collision between two elements or system paths)
     */
    public boolean register(final String pathPrefix) {
        return registeredPrefixes.add(pathPrefix);
    }

    /**
     * Deregisters a previously registered path prefix. Safe to call concurrently.
     *
     * @param pathPrefix the raw path prefix to remove
     */
    public void deregister(final String pathPrefix) {
        registeredPrefixes.remove(pathPrefix);
    }

    /**
     * Checks whether a proposed context path conflicts with any registered prefix.
     *
     * @param contextPath the proposed context path
     * @return the {@link PathConflict} classification
     */
    public PathConflict checkConflict(final String contextPath) {
        final var ctxWithSlash = withSlash(contextPath);
        var hasOuterConflict = false;
        for (final var prefix : registeredPrefixes) {
            final var prefixWithSlash = withSlash(prefix);
            if (ctxWithSlash.startsWith(prefixWithSlash)) return PathConflict.INNER;
            if (prefixWithSlash.startsWith(ctxWithSlash)) hasOuterConflict = true;
        }
        return hasOuterConflict ? PathConflict.OUTER : PathConflict.NONE;
    }

    /**
     * Returns {@code true} if the given request path falls under any registered prefix.
     * Used at request time by {@link StaticContentLoader} to skip reserved paths.
     *
     * @param requestPath the incoming request path
     * @return {@code true} if the path is covered by a registered prefix
     */
    public boolean isRegisteredPath(final String requestPath) {
        final var pathWithSlash = withSlash(requestPath);
        for (final var prefix : registeredPrefixes) {
            if (pathWithSlash.startsWith(withSlash(prefix))) return true;
        }
        return false;
    }

    private static String withSlash(final String path) {
        return path.endsWith("/") ? path : path + "/";
    }

}
