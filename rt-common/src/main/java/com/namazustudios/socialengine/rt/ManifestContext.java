package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;
import com.namazustudios.socialengine.rt.manifest.startup.StartupManifest;

/**
 * Loads the various manifest types over the network to be used by dependent services.
 */
@Proxyable
public interface ManifestContext {

    /**
     * Starts this {@link ManifestContext}.
     */
    default void start() {}

    /**
     * Stops this {@link ManifestContext}.
     */
    default void stop() {}

    /**
     * Gets the {@link ModelManifest} instance.
     *
     * @return the {@link ModelManifest}
     */
    @RemotelyInvokable
    ModelManifest getModelManifest();

    /**
     * Gets the manifest for the HTTP mappings, if available.
     *
     * @return the {@link HttpManifest}
     */
    @RemotelyInvokable
    HttpManifest getHttpManifest();

    /**
     * Gets the {@link SecurityManifest}, if available.
     *
     * @return the {@link SecurityManifest}
     */
    @RemotelyInvokable
    SecurityManifest getSecurityManifest();

    /**
     * Gets the {@link StartupManifest}, if available.
     *
     * @return the {@link StartupManifest}
     */
    @RemotelyInvokable
    StartupManifest getStartupManifest();

}
