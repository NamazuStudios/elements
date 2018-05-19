package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

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
     * Returns the immutable and globally-unique ID of this resource.  Though a resource
     * may exist at any path, this is the resource's ID.  All resources are assigned
     * a unique ID upon creation.  The ID must remain unique for the life of the
     * resource.
     *
     * @return the resource's ID
     */
    ResourceId getId();

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
     * Resumes a suspended task, accepting the task id.  The task will be resumed as soon as possible.  This is used
     * when the network resumes the call.  This is assumed that the resume was successful.  The response to the,
     * invocation whatever it may be, was successful.
     *
     * @param taskId the {@link TaskId} if the running task
     * @param result the resulting object from the call
     *
     */
    void resumeFromNetwork(final TaskId taskId, final Object result);

    /**
     * Resumes a suspended task, accepting the task id.  The task will be resumed as soon as possible.  This is used
     * when suspended coroutine encountered an error.  The coroutine will be resume with the error that caused the
     * underlying failure.
     *
     * @param taskId
     * @param throwable
     */
    void resumeWithError(final TaskId taskId, final Throwable throwable);

    /**
     * Resumes a suspended task, accepting the task id.  The task will be resumed as soon as possible.  This method
     * must succeed at the scheduling process, or else throw an exception.  Note that this does not guarantee successful
     * execution of the task, but rather successful scheduling.
     *
     * @param taskId the {@link TaskId} if the running task
     * @param elapsedTime the amount of time elapsed since the task was last activated
     *
     */
    void resumeFromScheduler(final TaskId taskId, final double elapsedTime);

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
     * Closes and destroys this Resource.  A resource, once destroyed, cannot be used again.
     */
    void close();

}
