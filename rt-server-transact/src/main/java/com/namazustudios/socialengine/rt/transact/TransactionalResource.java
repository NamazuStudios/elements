package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.DeadResource;
import com.namazustudios.socialengine.rt.MethodDispatcher;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.id.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.id.ResourceId.randomResourceId;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;

public class TransactionalResource implements Resource {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalResource.class);

    public static final int NASCENT_MAGIC = MIN_VALUE;

    public static final int TOMBSTONE_MAGIC = MIN_VALUE + 1;

    private static final TransactionalResource tombstone = new TransactionalResource() {
        @Override
        public boolean acquire() { throw new ResourceNotFoundException(); }
        @Override
        public int release() { return 0; }
    };

    private static final Consumer<TransactionalResource> ON_DESTROY_DEAD = _t -> {};

    private final ResourceId resourceId;

    private final AtomicReference<Consumer<TransactionalResource>> onDestroy;

    private final Resource delegate;

    private final AtomicInteger acquires = new AtomicInteger(MIN_VALUE);

    /**
     * Gets the tombstone {@link TransactionalResource}. This is a {@link TransactionalResource} that is used as
     * placeholder when removing resources from the internal cache.
     *
     * @return the {@link TransactionalResource} that serves as a tombstone marker
     */
    public static TransactionalResource getTombstone() {
        return tombstone;
    }

    private TransactionalResource() {
        this.resourceId = randomResourceId();
        this.acquires.set(TOMBSTONE_MAGIC);
        this.delegate = DeadResource.getInstance();
        this.onDestroy = new AtomicReference<>(ON_DESTROY_DEAD);
    }

    /**
     * Creates a new instance of {@link TransactionalResource} with the supplied {@link Runnable} which will execute
     * when the last reference has been released.
     *
     * @param delegate the delegate backs this {@link TransactionalResource}
     * @param onDestroy the routine which defines the on-destroy operation, guaranteed to only be run once.
     */
    public TransactionalResource(final Resource delegate,
                                 final Consumer<TransactionalResource> onDestroy) {
        this.resourceId = delegate.getId();
        this.delegate = delegate;
        this.onDestroy = new AtomicReference<>(onDestroy);
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
        return acquireAndGet() > 0;
    }

    public int acquireAndGet() {

        final int value = acquires.updateAndGet(i -> {
            if (i == 0)
                return 0;
            else if (i == NASCENT_MAGIC)
                return 1;
            else
                return i + 1;
        });

        return value;

    }

    /**
     * Releases this {@link Resource} by decrementing the reference count.  Once the count hits zero, the associated.
     * onDestroy routine will execute.  Once a {@link TransactionalResource} has been released to zero it will not
     * longer be {@link #acquire()}-able.  The first thread to set the acquire count to zero will immediately execute
     * the associated destruction routine and subsequent threads will simply skip any sort of destruction.
     *
     * @return true the value of acquires after the operation has been applied
     */
    public int release() {

        final int value = acquires.updateAndGet(i -> {

            if (i == NASCENT_MAGIC) {
                throw new IllegalStateException("Can't release nascent resource.");
            }

            if (i == 0) {
                return 0;
            }

            final int next = i - 1;

            if (next < 0) {
                throw new IllegalStateException("Unbalanced acquire/release.");
            } else {
                return next;
            }

        });

        if (value == TOMBSTONE_MAGIC) {
            final Consumer<TransactionalResource> onDestroy = this.onDestroy.getAndSet(ON_DESTROY_DEAD);
            onDestroy.accept(this);
        }

        return value;

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

    /**
     * Returns true if this instance is a tombstone, false otherwise.
     *
     * @return true if tombstone, false otherwise
     */
    public boolean isTombstone() {
        return acquires.get() == TOMBSTONE_MAGIC;
    }

    @Override
    public ResourceId getId() {
        return resourceId;
    }

    @Override
    public Attributes getAttributes() {
        return getDelegate().getAttributes();
    }

    @Override
    public MethodDispatcher getMethodDispatcher(String name) {
        return getDelegate().getMethodDispatcher(name);
    }

    @Override
    public void resume(TaskId taskId, Object... results) {
        getDelegate().resume(taskId, results);
    }

    @Override
    public void resumeFromNetwork(TaskId taskId, Object result) {
        getDelegate().resumeFromNetwork(taskId, result);
    }

    @Override
    public void resumeWithError(TaskId taskId, Throwable throwable) {
        getDelegate().resumeWithError(taskId, throwable);
    }

    @Override
    public void resumeFromScheduler(TaskId taskId, double elapsedTime) {
        getDelegate().resumeFromScheduler(taskId, elapsedTime);
    }

    @Override
    public void serialize(OutputStream os) throws IOException {
        getDelegate().serialize(os);
    }

    @Override
    public void deserialize(InputStream is) throws IOException {
        getDelegate().deserialize(is);
    }

    @Override
    public void serialize(WritableByteChannel wbc) throws IOException {
        delegate.serialize(wbc);
    }

    @Override
    public void deserialize(ReadableByteChannel is) throws IOException {
        getDelegate().deserialize(is);
    }

    @Override
    public void setVerbose(boolean verbose) {
        getDelegate().setVerbose(verbose);
    }

    @Override
    public boolean isVerbose() {
        return getDelegate().isVerbose();
    }

    @Override
    public Set<TaskId> getTasks() {
        return getDelegate().getTasks();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public void unload() {
        delegate.unload();
    }

    public Resource getDelegate() {

        if (acquires.get() == 0)
            throw new IllegalStateException("Resource is toast.");

        return delegate;

    }

}
