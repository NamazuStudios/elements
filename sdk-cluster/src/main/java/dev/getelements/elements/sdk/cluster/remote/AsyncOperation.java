package dev.getelements.elements.sdk.cluster.remote;

import dev.getelements.elements.sdk.cluster.remote.annotation.Dispatch.Type;
import dev.getelements.elements.sdk.cluster.remote.annotation.RemotelyInvokable;
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation;

import java.util.List;

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
     * Used as a default return value when implementing methods that return an instance of {@link AsyncOperation}.
     */
    AsyncOperation DEFAULT = () -> {};

}
