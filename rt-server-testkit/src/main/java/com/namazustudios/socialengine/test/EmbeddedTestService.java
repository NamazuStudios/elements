package com.namazustudios.socialengine.test;

import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.Worker;

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
     * Gets the client {@link Instance}.
     *
     * @return the client {@link Instance}
     */
    Instance getClient();

    /**
     * Gets the worker {@link Worker}.
     *
     * @return the worker {@link Worker}
     */
    Worker getWorker();

    /**
     * Gets the worker {@link Instance}.
     *
     * @return the worker {@link Instance}
     */
    Instance getWorkerInstance();

    /**
     * Closes the test service.
     */
    @Override
    void close();

    /**
     * Returns an {@link IocResolver} which can be used to access internal services bound to the client instance.
     *
     * @return the client {@link IocResolver}
     */
    IocResolver getClientIocResolver();

    /**
     * Return an {@link IocResolver} which can be used toa access internal services bound to the worker instance.
     *
     * @return the client {@link IocResolver}
     */
    IocResolver getWorkerIocResolver();

}
