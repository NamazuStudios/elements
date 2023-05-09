package dev.getelements.elements.test;

import dev.getelements.elements.rt.IocResolver;
import dev.getelements.elements.rt.Subscription;
import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.remote.Instance;

import java.util.function.Consumer;

/**
 * Base interface for an embedded {@link Instance} container.
 */
public interface EmbeddedInstanceContainer extends AutoCloseable {

    /**
     * Gets the {@link Instance} managed by this container.
     *
     * @return the instance
     */
    Instance getInstance();

    /**
     * Gets the {@link InstanceId} assigned to this container.
     * @return
     */
    default InstanceId getInstanceId() {
        return getInstance().getInstanceId();
    }

    /**
     * Starts this {@link EmbeddedWorkerInstanceContainer}.
     *
     * @return this instance
     */
    EmbeddedInstanceContainer start();

    /**
     * Registers for a notification when this instance closes.
     *
     * @param consumer the consumer which receives this event
     * @return an instance of {@link Subscription}
     */
    Subscription onClose(Consumer<? super EmbeddedInstanceContainer> consumer);

    /**
     * Closes this {@link EmbeddedWorkerInstanceContainer}.
     */
    void close();

}
