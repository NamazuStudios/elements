package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.Method;

/**
 * Contains the result of the remote invocation.
 */
public class InvocationResult {

    private boolean ok;

    private Object result;

    private Throwable throwable;

    /**
     * Returns true if the remote method executed successfully.,
     *
     * @return true if okay, false otherwise
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * Sets the flag to represent successful remote invocation.
     *
     * @param ok true if successful, false otherwise
     */
    public void setOk(boolean ok) {
        this.ok = ok;
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

    /**
     * Gets the {@link Throwable} thrown by the remote {@link Method}, or null if the method executed successfully.
     *
     * @return the result
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Sets the {@link Throwable} thrown by the remote {@link Method}, or null if the method executed successfully.
     *
     * @param throwable  the result
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

}
