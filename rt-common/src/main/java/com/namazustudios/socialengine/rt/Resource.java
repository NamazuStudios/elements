package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.id.TaskId;
import com.namazustudios.socialengine.rt.exception.ResourceDestroyedException;
import com.namazustudios.socialengine.rt.util.InputStreamAdapter;
import com.namazustudios.socialengine.rt.util.OutputStreamAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Set;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.util.OutputStreamAdapter.FLUSH;
import static java.nio.ByteBuffer.allocate;

/**
 * A {@link Resource} is a logical unit of work, which is represented by an instance of this type.  It essentially
 * contains a set of handler methods, which can be invoked using the {@link #getMethodDispatcher(String)} method as well
 * as keep running tasks, as represented by {@link TaskId}.
 *
 * This interface is meant to be implemented directly or, for clustered configurations, a proxy to a remote
 * {@link Resource}.  Therefore, it is necessary to restrict the methods and return values on this interface to objects
 * and types which can be serialized (ie plain-old-data), or can be used as proxies themselves.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface Resource extends AutoCloseable {

    /**
     * Default IO Buffer size, 4kb
     */
    int DEFAULT_IO_BUFFER_SIZE = 4096;

    /**
     * Returns the immutable and globally-unique ID of this resource.  Though a resource
     * may exist at any path, this is the resource's ID.  All resources are assigned
     * a unique ID upon creation.  The ID must remain unique for the life of the
     * resource.
     *
     * @return the resource's ID
     */
    ResourceId getId();

    /**
     * Gets the {@link Attributes} associated with this {@link Resource}
     *
     * @return this instance's {@link Attributes}
     */
    MutableAttributes getAttributes();

    /**
     * Returns an instance of {@link MethodDispatcher}, which is used to invoke methods against this {@link Resource}.
     * The reurned {@link MethodDispatcher} will defer actually invoking the method until the final call in the
     * chain {@link ResultAcceptor#dispatch(Consumer, Consumer)}.
     *
     * @param name the name of the method
     * @return the {@link MethodDispatcher}, never null
     *
     * @throws {@link MethodNotFoundException} if the method cannot be found
     */
    MethodDispatcher getMethodDispatcher(String name);

    /**
     * Resumes the supplied task with the {@link TaskId} and the list of results.
     *
     * @param taskId the {@link TaskId} of the associated task
     * @param results zero or more results supplied to the resumed task
     */
    void resume(final TaskId taskId, final Object ... results);

    /**
     * Resumes a suspended task, accepting the task id.  The task will be resumed as soon as possible.  This is used
     * when the network resumes the call.  This is assumed that the resume was successful.  The response to the,
     * invocation whatever it may be, was successful.
     *
     * @param taskId the {@link TaskId} if the running task
     * @param result the resulting object from the call
     *
     */
    default void resumeFromNetwork(final TaskId taskId, final Object result) {
        resume(taskId, ResumeReason.NETWORK, result);
    }

    /**
     * Resumes a suspended task, accepting the task id.  The task will be resumed as soon as possible.  This is used
     * when suspended coroutine encountered an error.  The coroutine will be resume with the error that caused the
     * underlying failure.
     *
     * @param taskId
     * @param throwable
     */
    default void resumeWithError(final TaskId taskId, final Throwable throwable) {
        resume(taskId, ResumeReason.ERROR, throwable);
    }

    /**
     * Resumes a suspended task, accepting the task id.  The task will be resumed as soon as possible.  This method
     * must succeed at the scheduling process, or else throw an exception.  Note that this does not guarantee successful
     * execution of the task, but rather successful scheduling.
     *
     * @param taskId the {@link TaskId} if the running task
     * @param elapsedTime the amount of time elapsed since the task was last activated
     *
     */
    default void resumeFromScheduler(final TaskId taskId, final double elapsedTime) {
        resume(taskId, ResumeReason.SCHEDULER, elapsedTime);
    }
    
    /**
     * Dumps the entire contents of this {@link Resource} to the supplied {@link OutputStream} where it can be
     * reconstituted later using the {@link #deserialize(InputStream)} method.
     *
     * @param os the {@link OutputStream} used to receive the serialized {@link Resource}
     * @throws IOException if something failed during serialization
     */
    void serialize(final OutputStream os) throws IOException;

    /**
     * Restores the entire state of this {@link Resource} from the supplied {@link InputStream}.  This assumes the
     * {@link InputStream} was produced by a call to {@link #serialize(OutputStream)}.
     *
     * @param is the {@link InputStream} from which to read the serialized resource
     * @throws IOException if something failed during deserialization
     */
    void deserialize(final InputStream is) throws IOException;

    /**
     * Dumps the entire contents of this {@link Resource} to the supplied {@link WritableByteChannel} where it can be
     * reconstituted later using the {@link #deserialize(InputStream)} method.
     *
     * @param wbc the {@link OutputStream} used to receive the serialized {@link Resource}
     * @throws IOException if something failed during serialization
     */
    default void serialize(final WritableByteChannel wbc) throws IOException {

        final ByteBuffer byteBuffer = allocate(DEFAULT_IO_BUFFER_SIZE);

        try (final OutputStream os = new OutputStreamAdapter(wbc, byteBuffer, FLUSH)) {
            serialize(os);
        }

    }

    /**
     * Restores the entire state of this {@link Resource} from the supplied {@link InputStream}.  This assumes the
     * {@link InputStream} was produced by a call to {@link #serialize(OutputStream)}.
     *
     * @param is the {@link InputStream} from which to read the serialized resource
     * @throws IOException if something failed during deserialization
     */
    default void deserialize(final ReadableByteChannel ibc) throws IOException {

        final ByteBuffer byteBuffer = allocate(DEFAULT_IO_BUFFER_SIZE);

        try (final InputStream is = new InputStreamAdapter(ibc, byteBuffer, FLUSH)) {
            deserialize(is);
        }

    }

    /**
     * Sets the verbose mode.  This will enable enhanced logging for debug purposes.
     *
     * @param verbose true if verbose, false otherwise
     */
    default void setVerbose(boolean verbose) {}

    /**
     * Returns true if the resource is set to enable verbose mode.
     *
     * @return true if verbose mode is enabled.
     */
    default boolean isVerbose() {return false; }

    /**
     * Gets a {@link Set<TaskId>} representing active tasks at the current state.
     *
     * @return the running tasks as a {@link Set}
     */
    Set<TaskId> getTasks();

    /**
     * Closes and destroys this Resource.  A resource, once destroyed, cannot be used again.  Any tasks pending on the
     * resource will be completed with a {@link ResourceDestroyedException} immediately.  This is simlar to
     * {@link #close()} in that it frees up memory associated with this {@link Resource}.  However, its key difference
     * is that it also propagates exceptions which indicate that it has reached a final state.  In contrast to
     * {@link #unload()} which indicates that the {@link Resource} my be reconstituted later to continue performing
     * work.
     */
    void close();

    /**
     * Closes and destroys this Resource.  A resource, once destroyed, cannot be used again.  Any tasks pending on ths
     * resource will not be destroyed or unregistered.  It is possible that later a new {@link Resource} would be made
     * and the contents of this deserialized back into this one.
     */
    void unload();

}
