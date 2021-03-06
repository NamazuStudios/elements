package com.namazustudios.socialengine.test;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.Worker;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a completely and entirely separate test kit. This is an interface to a complete and total embedded test
 * instance containing a client as well as a single worker instance in a single memory space.
 *
 * This allows test code to access both a client and worker instance {@link Context} which can be used to issue calls
 * to the underlying script engine.
 *
 * In the case of the client {@link Context}, the calls are serialized over a virtual network ensuring that the behavior
 * of the simulated cluster resembles the actual deployed behavior as closely as absolutely possible.
 */
public interface EmbeddedTestService extends AutoCloseable {

    /**
     * Starts the {@link EmbeddedTestService}.
     *
     * @return
     */
    EmbeddedTestService start();

    /**
     * Subscribes to an even that is fired when the service is closed.
     *
     * @param consumer
     * @return
     */
    Subscription onClose(Consumer<? super EmbeddedTestService> consumer);

    /**
     * Closes the test service.
     */
    @Override
    void close();

    @Deprecated
    default IocResolver getIocResolver() {
        throw new UnsupportedOperationException("Deprecated.");
    }

    @Deprecated
    default IocResolver getClientIocResolver() {
        throw new UnsupportedOperationException("Deprecated.");
    }

    /**
     * Gets the client {@link EmbeddedInstanceContainer}.
     *
     * @return the client {@link EmbeddedInstanceContainer}
     */
    default EmbeddedClientInstanceContainer getClient() {
        return getClientOptional().orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the client {@link Optional<EmbeddedInstanceContainer>}. Is present only if configured.
     *
     * @return the client {@link Optional<EmbeddedInstanceContainer>}
     */
    Optional<EmbeddedClientInstanceContainer> getClientOptional();

    /**
     * Gets the worker {@link EmbeddedWorkerInstanceContainer}.
     *
     * @return the worker {@link EmbeddedWorkerInstanceContainer}
     */
    default EmbeddedWorkerInstanceContainer getWorker() {
        return getWorkerOptional().orElseThrow(IllegalStateException::new);
    }

    /**
     * Gets the client {@link Optional<EmbeddedWorkerInstanceContainer>}. Is present only if configured.
     *
     * @return the client {@link Optional<EmbeddedWorkerInstanceContainer>}
     */
    Optional<EmbeddedWorkerInstanceContainer> getWorkerOptional();

}
