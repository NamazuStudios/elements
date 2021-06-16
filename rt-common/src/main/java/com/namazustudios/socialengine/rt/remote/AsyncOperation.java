package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.annotation.Dispatch.Type;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**'
 * A special return type which can be returnd by methods annotated with {@link RemotelyInvokable} which also meet the
 * requirements for having the {@link Type#ASYNCHRONOUS}. When a method is backed by,
 * {@link RemoteInvoker#invokeAsyncV(Invocation, List, InvocationErrorConsumer)} the result will be returned to the
 * calling code which can control the underlying remote invocation. This controls the underlying connection state and
 * allows the calling code to set timeouts or cancel the pending invocation.
 */
public interface AsyncOperation {

    /**
     * Cancels the invocation.
     */
    void cancel();

    /**
     * Sets the operation to automatically cancel after the supplied time has elapsed.
     *
     * @param time
     * @param timeUnit
     */
    void timeout(long time, TimeUnit timeUnit);

}
