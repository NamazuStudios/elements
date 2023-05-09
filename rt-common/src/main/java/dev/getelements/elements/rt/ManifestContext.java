package dev.getelements.elements.rt;

import dev.getelements.elements.rt.annotation.Proxyable;
import dev.getelements.elements.rt.annotation.RemoteService;
import dev.getelements.elements.rt.annotation.RemoteScope;
import dev.getelements.elements.rt.annotation.RemotelyInvokable;
import dev.getelements.elements.rt.manifest.http.HttpManifest;
import dev.getelements.elements.rt.manifest.model.ModelManifest;
import dev.getelements.elements.rt.manifest.security.SecurityManifest;
import dev.getelements.elements.rt.manifest.startup.StartupManifest;

import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static dev.getelements.elements.rt.annotation.RemoteScope.WORKER_SCOPE;

/**
 * Loads the various manifest types over the network to be used by dependent services.
 */
@Proxyable
@RemoteService(scopes = @RemoteScope(scope = WORKER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL))
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
