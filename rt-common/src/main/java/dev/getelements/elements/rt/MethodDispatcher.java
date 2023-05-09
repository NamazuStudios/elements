package dev.getelements.elements.rt;

/**
 * Dispatches invocations of methods against {@link Resource} instances.  This accepts
 * parameters and then provides a means to handle the response to the invocation.
 */
@FunctionalInterface
public interface MethodDispatcher {

    /**
     * Returns an instance of {@link ResultAcceptor}
     *
     * @param parameters
     * @return
     */
    ResultAcceptor<Object> params(final Object ... parameters);

}
