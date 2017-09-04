package com.namazustudios.socialengine.rt;

/**
 * Dispatches invocations of methods against {@link Resource} instances.  This accepts
 * parameters and then provides a means to handle the response to the invocation.
 */
public interface MethodDispatcher {

    /**
     * Returns an instance of {@link ResultAcceptor}
     *
     * @param parameters
     * @return
     */
    ResultAcceptor<Object> dispatch(Object ... parameters);

}
