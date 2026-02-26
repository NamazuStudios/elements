package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;

/**
 * Runs a local instance of Elements suitable for debugging. This works by interacting with the local build system to
 * ensure that the artifacts are installed and then loading them directly from the result of the build system. This
 * is intended to be used stand alone, or in conjunction with an IDE for debugging. In an IDE, it is strongly
 * recommended that the local SDK be the only entry on the runtime classpath and let the artifact loader system read the
 * installed artifacts from the local repository.
 */
public interface ElementsLocal extends AutoCloseable {

    /**
     * Starts this {@link ElementsLocal}.
     *
     * @return this instance
     */
    ElementsLocal start();

    /**
     * Runs the instance and will block until shutdown.
     *
     * @return this instance
     */
    ElementsLocal run();

    /**
     * Gets the {@link ElementRuntimeService} for this {@link ElementsLocal} instance. This can be used to load and
     * unload Elements from the runtime on the fly.
     *
     * @return the {@link ElementRuntimeService}
     */
    ElementRuntimeService getRuntimeService();

    /**
     * Gets the root {@link ElementRegistry} used by the local runner.
     *
     * @return the {@link ElementRegistry}
     */
    MutableElementRegistry getRootElementRegistry();

    /**
     * Closes this {@link ElementsLocal}.
     */
    @Override
    void close();

}
