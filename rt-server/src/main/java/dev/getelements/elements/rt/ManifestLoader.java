package dev.getelements.elements.rt;

import dev.getelements.elements.rt.manifest.event.EventManifest;
import dev.getelements.elements.rt.manifest.model.ModelManifest;
import dev.getelements.elements.rt.manifest.startup.StartupManifest;

/**
 * Created by patricktwohig on 8/14/17.
 */
public interface ManifestLoader  {

    /**
     * Gets the {@link ModelManifest} instance.
     *
     * @return the {@link ModelManifest}
     */
    ModelManifest getModelManifest();

    /**
     * Gets the {@link StartupManifest}, if available.
     *
     * @return the {@link StartupManifest}
     */
    StartupManifest getStartupManifest();

    /**
     * Gets the {@link EventManifest}, if available.
     *
     * @return the {@link EventManifest}
     */
    EventManifest getEventManifest();

    /**
     * Returns whether or not the manifest loader has already loaded and closed the lua manifest.
     * @return Whether or not the manifest has loaded and closed the manifest.
     */
    boolean getClosed();

}
