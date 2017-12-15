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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvocationResult)) return false;

        InvocationResult that = (InvocationResult) o;

        if (getArg() != that.getArg()) return false;
        return getResult() != null ? getResult().equals(that.getResult()) : that.getResult() == null;
    }

    @Override
    public int hashCode() {
        int result1 = getArg();
        result1 = 31 * result1 + (getResult() != null ? getResult().hashCode() : 0);
        return result1;
    }

    @Override
    public String toString() {
        return "InvocationResult{" +
                "arg=" + arg +
                ", result=" + result +
                '}';
    }

}
