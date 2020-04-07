package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.DeadResource;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.SimpleDelegateResource;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;

public class TransactionalResource extends SimpleDelegateResource {

    private static final int NASCENT_MAGIC = MIN_VALUE;

    private static final Consumer<TransactionalResource> ON_DESTROY_DEAD = _t -> {};

    private final AtomicReference<Revision<?>> revision;

    private final AtomicReference<Consumer<TransactionalResource>> onDestroy;

    private final AtomicInteger acquires = new AtomicInteger(MIN_VALUE);

    /**
     * Creates a new instance of {@link TransactionalResource} with the supplied {@link Runnable} which will execute
     * when the last reference has been released.
     *
     * @param delegate the delegate backs this {@link TransactionalResource}
     * @param onDestroy the routine which defines the on-destroy operation, guaranteed to only be run once.
     */
    public TransactionalResource(final Revision<?> revision,
                                 final Resource delegate,
                                 final Consumer<TransactionalResource> onDestroy) {
        super(delegate);
        this.revision = new AtomicReference<>(revision);
        this.onDestroy = new AtomicReference<>(onDestroy);
    }

    /**
     * Gets the {@link Revision<?>} of this {@link Resource}.
     *
     * @return the {@link Revision<?>}
     */
    public Revision<?> getRevision() {
        return revision.get();
    }

    /**
     * Acquires the {@link Resource} by incrementing the reference count.  If this {@link TransactionalResource} is in
     * its nascent state, this will set the reference count to 1.
     *
     * If the acquire count is at 0, then we attempted to acquire just after somebody else cleared the count.
     * Therefore, the calling code must handle this edge case by treating it as if the resource doesn't exist.
     *
     * @return true if this Resource is still active, false otherwise
     */
    public boolean acquire() {
        final int value = acquires.updateAndGet(i -> i == NASCENT_MAGIC ? 1 : i == 0 ? 0 : i + 1);
        return value == 0;
    }

    /**
     * Releases this {@link Resource} by decrementing the reference count.  Once the count hits zero, the associated.
     * onDestroy routine will execute.  Once a {@link TransactionalResource} has been released to zero it will not
     * longer be {@link #acquire()}-able.  The first thread to set the acquire count to zero will immediately execute
     * the associated destruction routine and subsequent threads will simply skip any sort of destruction.
     *
     * @return true if the resource is still active, was still active prior to the invocation of this method, or false
     *         otherwise.
     */
    public boolean release() {

        final int value = acquires.updateAndGet(i -> max(0, i - 1));

        if (value == 0) {

            // Executes the object's destructor to the singleton and executes the destructor that was already in
            // the atomic reference to the destructor.
            final Consumer<TransactionalResource> onDestroy = this.onDestroy.getAndSet(ON_DESTROY_DEAD);
            onDestroy.accept(this);

            // Tests if the destructor was supplied to the constructor of this object, or if it is the singleton
            // dead on-destroy routine.  If it was indeed the object's destructor then we consider the operation
            // a success and return true.
            return onDestroy != ON_DESTROY_DEAD;
        } else {
            return true;
        }

    }

    /**
     * Checks if this {@link TransactionalResource} is in the nascent state.  That is, the {@link Resource} has
     * recently been loaded, but not acquired for the first time.
     *
     * @return true if this is in a nascent state, false otherwise
     */
    public boolean isNascent() {
        return acquires.get() == NASCENT_MAGIC;
    }

    public static TransactionalResource deadResource(final Consumer<TransactionalResource> onDestroy) {
        return new TransactionalResource(Revision.zero(), DeadResource.getInstance(), onDestroy) {

            @Override
            public boolean acquire() {
                return false;
            }

            @Override
            public boolean isNascent() {
                return false;
            }

        };
    }

}
