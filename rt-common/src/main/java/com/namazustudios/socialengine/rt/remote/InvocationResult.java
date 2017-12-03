package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.Method;

/**
 * Contains the result of the remote invocation.
 */
public class InvocationResult {


    private int arg;

    private Object result;

    /**
     * Gets the argument position which accepts the result.
     *
     * @return the argument.
     */
    public int getArg() {
        return arg;
    }

    /**
     * Sets the argument position which accepts the result.
     *
     * @param arg  the argument.
     */
    public void setArg(int arg) {
        this.arg = arg;
    }

    /**
     * Gets the result of the remote {@link Method}, or null if the method failed to invoke.  Null may also indicate
     * that the remote method returned null.
     *
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * Sets the result of the remote {@link Method}, or null if the method failed to invoke.  Null may also indicate
     * that the remote method returned null.
     *
     * @return the result
     */
    public void setResult(Object result) {
        this.result = result;
    }

}
