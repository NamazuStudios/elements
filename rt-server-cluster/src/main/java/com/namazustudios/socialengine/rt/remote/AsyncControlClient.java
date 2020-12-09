package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.function.Consumer;
import java.util.function.Function;

public interface AsyncControlClient extends AutoCloseable {

    /**
     * Gets the {@link InstanceId} for the remote instance.
     *
     * @param responseConsumer the {@link Consumer<Response<InstanceStatus>>} to handle the request
     * @return the {@link Request} for the instance status
     */
    Request getInstanceStatus(Consumer<Response<? extends InstanceStatus>> responseConsumer);

    /**
     * Issues the command to open up a route to the node.
     *
     * @param nodeId the {@link NodeId}
     * @param instanceInvokerAddress the remote instance invoker address
     * @param responseConsumer the {@link Consumer<Response<String>>} to handle the request
     * @return the connect address for the node
     */
    Request openRouteToNode(NodeId nodeId, String instanceInvokerAddress, Consumer<Response<String>> responseConsumer);

    /**
     * Close the route to the {@link NodeId}.  If the route is not known, then nothing happens.
     *
     * @param nodeId the {@link NodeId}
     *
     */
    Request closeRouteToNode(NodeId nodeId, Consumer<Response<Void>> responseConsumer);

    /**
     * Close the routes via the {@link InstanceId}.  If no routes are known, then nothing happens.
     *
     * @param instanceId the {@link InstanceId}
     *
     */
    Request closeRoutesViaInstance(InstanceId instanceId, Consumer<Response<Void>> responseConsumer);

    /**
     * Opens an {@link InstanceConnectionService.InstanceBinding} provided the {@link NodeId} and returns the
     * {@link InstanceConnectionService.InstanceBinding}.
     *
     * @param nodeId the {@link NodeId}
     * @return the {@link InstanceConnectionService.InstanceBinding}
     */
    Request openBinding(NodeId nodeId,
                        Consumer<Response<? extends InstanceConnectionService.InstanceBinding>> responseConsumer);

    /**
     * Issues the command to close a binding.  This will invalidate all
     * {@link InstanceConnectionService.InstanceBinding}s to that {@link NodeId}.
     *
     * @param nodeId
     */
    Request closeBinding(NodeId nodeId, Consumer<Response<Void>> responseConsumer);

    /**
     * Closes this instance of {@link AsyncControlClient}. Any pending tasks will be failed with a callback to any
     * pending responses.
     */
    @Override
    void close();

    /**
     * Used to open on-demand instances of the {@link ControlClient}.
     */
    @FunctionalInterface
    interface Factory {

        /**
         * Opens a {@link ControlClient} with the supplied connect address.
         *
         * @param connectAddress the connect address
         * @return the {@link ControlClient}
         */
        ControlClient open(final String connectAddress);

    }

    /**
     * Represents a pending request that this {@link AsyncControlClient} is making.
     */
    interface Request {

        /**
         * Cancels the pending response, if possible.
         */
        void cancel();

    }

    /**
     * Represents a response produced by the remote end.
     *
     * @param <T> the result type
     */
    interface Response<T> {

        /**
         * Gets the response, throwing the appropriate {@link Exception} if there was a problem processing the Request
         *
         * @return the result
         */
        T get() throws BaseException;

        /**
         * Maps this {@link Response} to the new type using the supplied {@link Function<T, U>}
         * @param mapper the mapper function
         * @param <U> the desired type
         * @return a {@link Response<U>}
         */
        default <U> Response<U> map(final Function<T, U> mapper) {
            return () -> mapper.apply(Response.this.get());
        }

    }

}
