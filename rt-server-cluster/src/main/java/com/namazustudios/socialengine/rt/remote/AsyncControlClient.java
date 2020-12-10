package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;

import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface AsyncControlClient extends AutoCloseable {

    /**
     * Gets the {@link InstanceId} for the remote instance.
     *
     * @param responseConsumer the {@link Consumer<Response<InstanceStatus>>} to handle the request
     * @return the {@link Request} for the instance status
     */
    Request getInstanceStatus(ResponseConsumer<InstanceStatus> responseConsumer);

    /**
     * Issues the command to open up a route to the node.
     *
     * @param nodeId the {@link NodeId}
     * @param instanceInvokerAddress the remote instance invoker address
     * @param responseConsumer the {@link Consumer<Response<String>>} to handle the request
     * @return the connect address for the node
     */
    Request openRouteToNode(NodeId nodeId, String instanceInvokerAddress, ResponseConsumer<String> responseConsumer);

    /**
     * Close the route to the {@link NodeId}.  If the route is not known, then nothing happens.
     *
     * @param nodeId the {@link NodeId}
     *
     */
    Request closeRouteToNode(NodeId nodeId, ResponseConsumer<Void> responseConsumer);

    /**
     * Close the routes via the {@link InstanceId}.  If no routes are known, then nothing happens.
     *
     * @param instanceId the {@link InstanceId}
     *
     */
    Request closeRoutesViaInstance(InstanceId instanceId, ResponseConsumer<Void> responseConsumer);

    /**
     * Opens an {@link InstanceBinding} provided the {@link NodeId} and returns the
     * {@link InstanceBinding}.
     *
     * @param nodeId the {@link NodeId}
     * @return the {@link InstanceBinding}
     */
    Request openBinding(NodeId nodeId, ResponseConsumer<InstanceBinding> responseConsumer);

    /**
     * Issues the command to close a binding.  This will invalidate all
     * {@link InstanceBinding}s to that {@link NodeId}.
     *
     * @param nodeId
     */
    Request closeBinding(NodeId nodeId, ResponseConsumer<Void> responseConsumer);

    default Request openBinding0(Consumer<Runnable> dispatch,
                                 NodeId nodeId,
                                 ResponseConsumer<InstanceBinding> responseConsumer) {
        final ResponseConsumer<InstanceBinding> wrapped = r -> dispatch.accept(() -> responseConsumer.accept(r));
        return openBinding(nodeId, wrapped);
    }

    /**
     * Configures this {@link AsyncControlClient} to dispatch the response handling to the supplied
     * {@link Consumer<Runnable>}. There are cases in which it may be desirable to dispatch the actual interactions
     * with a background thread or process.
     *
     * For example the async callbacks to the underlying types may be using one of a few critical IO threads, it would
     * be imperative to avoid blocking such threads. Therefore, the operations may dispatch to the supplied dispatcher.
     *
     * @param dispatch accepts a {@link Runnable} to run at now or a greater point in time.
     * @return a wrapped {@link AsyncControlClient} which dispatches it's work to the supplied dispatch.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default AsyncControlClient withDispatch(final Consumer<Runnable> dispatch) {

        final ClassLoader classLoader = getClass().getClassLoader();
        final Class<?>[] interfaces = new Class<?>[]{AsyncControlClient.class};

        return (AsyncControlClient) Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {

            if (Request.class.isAssignableFrom(method.getReturnType())) {

                final Function<Object, Object> remapper = arg -> {
                    if (arg instanceof ResponseConsumer) {

                        final ResponseConsumer original = (ResponseConsumer) arg;

                        return (ResponseConsumer) response -> {
                            try {
                                final var result = response.get();
                                dispatch.accept(() -> original.accept(() -> result));
                            } catch (Exception ex) {
                                ex.fillInStackTrace();
                                dispatch.accept(() -> original.accept(() -> { throw ex; }));
                            }
                        };

                    } else {
                        return arg;
                    }
                };

                final var remapped = Stream.of(args).map(remapper).toArray();
                return method.invoke(this, remapped);

            } else {
                return method.invoke(this, args);
            }

        });

    }

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
        AsyncControlClient open(final String connectAddress);

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

    /**
     * Response
     *
     * @param <T>
     */
    @FunctionalInterface
    interface ResponseConsumer<T> {

        /**(
         * Accepts the response.
         *
         * @param tResponse
         */
        void accept(Response<? extends T> tResponse);

    }

}
