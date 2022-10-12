package com.namazustudios.socialengine.rt.jrpc;

import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcManifest;

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
