package com.namazustudios.socialengine.rt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents an asynchornous type of {@link Connection}.  Unless otherwise indicated, instances of
 * {@link AsyncConnection<ContextT, SocketT>} aren't thread-safe.  Therefore you must ensure that all manipulation of
 * the {@link AsyncConnection<ContextT, SocketT>} instance is performed within a callback from the subscribed events.
 */
public interface AsyncConnection<ContextT, SocketT> extends Connection<ContextT, SocketT> {

    /**
     * Sends a signal to this {@link AsyncConnection}. The signal will run on the {@link AsyncConnection}'s thread and
     * can be used to safely manipulate the {@link AsyncConnection} from an outside thread.
     *
     * This method, and other "signal" methods, may be invoked from any thread.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     */
    void signal(Consumer<AsyncConnection<ContextT, SocketT>> asyncConnectionConsumer);

    /**
     * Computes a value on the connection's IO Thread. This will return a {@link CompletionStage<T>} which may be used
     * to retrieve the value.
     *
     * @param asyncConnectionConsumer the consumer.
     * @param <T> the type to compute
     * @return the {@link CompletionStage<T>}
     */
    default <T> CompletionStage<T> signalAndComputeCompletion(
            final Function<AsyncConnection<ContextT, SocketT>, T> asyncConnectionConsumer) {

        final var future = new CompletableFuture<T>();

        try {
            signal(connection -> {
                try {
                    final var t = asyncConnectionConsumer.apply(connection);
                    future.complete(t);
                } catch (Exception ex) {
                    future.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            future.completeExceptionally(ex);
        }

        return future;

    }

    /**
     * Computes a value on the connection's IO Thread. This will return a {@link CompletionStage<Void>} which may be
     * used to block until the IO thread completes the supplied task.
     *
     * @param asyncConnectionConsumer the consumer to execute the task
     * @return the {@link CompletionStage<Void>}
     */
    default CompletionStage<Void> signalAndComputeCompletionV(
            final Consumer<AsyncConnection<ContextT, SocketT>> asyncConnectionConsumer) {
        return signalAndComputeCompletion(c -> {
            asyncConnectionConsumer.accept(c);
            return null;
        });
    }

    /**
     * Returns this {@link AsyncConnection<ContextT, SocketT>}.  This hints to the underlying
     * {@link AsyncConnection<ContextT, SocketT>Service} that the client code is done using the connection.  Depending
     * on circumstances, this may simply close the connection.
     */
    void recycle();

    /**
     * Clears all events until a subsequent call to {@link #setEvents(Event...)} is made.  This may be a shortcut for
     * simply passing no arguments to {@link #setEvents(Event...)}.
     */
    default void clearEvents() {
        setEvents();
    }

    /**
     * Ensures that the {@link AsyncConnection} is registered for the supplied events.
     *
     * @param events one of many {@link Event}s
     */
    void setEvents(Event ... events);

    /**
     * Registers a {@link Subscription} for when a socket has read data.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection<ContextT, SocketT>>} to receive the event
     * @return {@link Subscription}
     */
    
    Subscription onRead(Consumer<AsyncConnection<ContextT, SocketT>> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a socket is ready to write data.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection<ContextT, SocketT>>} to receive the event
     * @return {@link Subscription}
     */
    Subscription onWrite(Consumer<AsyncConnection<ContextT, SocketT>> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a socket has encountered an error.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection<ContextT, SocketT>>} to receive the event
     * @return {@link Subscription}
     */
    Subscription onError(Consumer<AsyncConnection<ContextT, SocketT>> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription for when the underlying connection was closed.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection<ContextT, SocketT>>} to receive the event
     * @return {@link Subscription}
     */
    Subscription onClose(Consumer<AsyncConnection<ContextT, SocketT>> asyncConnectionConsumer);

    /**
     * Subscribes to an event indicating that the connection was recycled.
     *
     * @param pooledAsyncConnectionConsumer the {@link Consumer<AsyncConnection<ContextT, SocketT>>} to receive the event.
     *
     * @return a {@link Subscription} to the event
     */
    Subscription onRecycle(Consumer<AsyncConnection<ContextT, SocketT>> pooledAsyncConnectionConsumer);

    /**
     * Enumeration of events for which to listen.
     */
    enum Event {

        /**
         * Corresponds to the calls related to {@link AsyncConnection#onRead(Consumer)}
         */
        READ,

        /**
         * Corresponds to the calls related to {@link AsyncConnection#onWrite(Consumer)}
         */
        WRITE,

        /**
         * Corresponds to the calls related to {@link AsyncConnection#onError(Consumer)}
         */
        ERROR

    }

}
