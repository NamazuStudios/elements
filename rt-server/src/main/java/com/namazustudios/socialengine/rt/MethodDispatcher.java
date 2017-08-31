package com.namazustudios.socialengine.rt;

import java.util.function.Consumer;

/**
 * Dispatches invocations of methods against {@link Resource} instances.  This accepts
 * parameters and then provides a means to handle the response to the invocation.
 */
public interface MethodDispatcher {

    /**
     * Returns an instance of {@link Acceptor}
     * @param parameters
     * @return
     */
    ResultAcceptor dispatch(Object ... parameters);

}
