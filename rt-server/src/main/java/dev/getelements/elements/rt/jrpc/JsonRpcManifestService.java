package dev.getelements.elements.rt.jrpc;

import dev.getelements.elements.rt.manifest.jrpc.JsonRpcManifest;

/**
 * Provides the {@link JsonRpcManifest}.
 */
public interface JsonRpcManifestService {

    /**
     * Gets the {@link JsonRpcManifestService}.
     *
     * @return a deep-copy of the {@link JsonRpcManifest}.
     */
    JsonRpcManifest getJsonRpcManifest();

}
