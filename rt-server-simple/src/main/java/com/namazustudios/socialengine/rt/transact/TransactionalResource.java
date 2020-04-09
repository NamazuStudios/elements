package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.id.TaskId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;

public class TransactionalResource implements Resource {

    private static final int NASCENT_MAGIC = MIN_VALUE;

    private static final Context DEAD_CONTEXT = new Context(DeadResource.getInstance(), Revision.infinity());

    private static final Consumer<TransactionalResource> ON_DESTROY_DEAD = _t -> {};

    private final AtomicReference<Context> context;

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
        this.onDestroy = new AtomicReference<>(onDestroy);
        this.context = new AtomicReference<>(new Context(delegate, revision));
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

    @Override
    public ResourceId getId() {
        return getDelegate().getId();
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
        getDelegate().serialize(wbc);
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
        getDelegate().unload();
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

    public Resource update(final Resource update, final Revision<?> revision) {

        final Context replacement = new Context(update, revision);

        boolean success;
        Context existing;

        do {
            existing = context.get();
            success = existing.revision.isBefore(revision);
        } while(!context.compareAndSet(existing, success ? replacement : existing));

        return success ? existing.delegate : update;

    }

    private Resource getDelegate() {
        final Context context = this.context.get();
        return context.delegate;
    }

    public Revision<?> getRevision() {
        final Context context = this.context.get();
        return context.revision;
    }

    public static class Context {

        private final Resource delegate;

        private final Revision<?> revision;

        public Context(Resource delegate, Revision<?> revision) {
            this.delegate = delegate;
            this.revision = revision;
        }

    }

}
