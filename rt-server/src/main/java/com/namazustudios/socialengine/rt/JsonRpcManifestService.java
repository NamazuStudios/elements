package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.remote.Invocation;

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
