package com.namazustudios.socialengine.test;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.Worker;

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

    /**
     * Gets the client {@link Instance}.
     *
     * @return the client {@link Instance}
     */
    EmbeddedInstanceContainer getClient();

    /**
     * Gets the worker {@link Worker}.
     *
     * @return the worker {@link Worker}
     */
    EmbeddedWorkerInstanceContainer getWorker();

    /**
     * Gets the {@link IocResolver} for the default application. This is implementation specific. Typically this means
     * it is the first-configured {@link com.namazustudios.socialengine.rt.id.ApplicationId}
     *
     * @return
     */
    IocResolver getWorkerIocResolver();

    IocResolver getClientIocResolver();

}
