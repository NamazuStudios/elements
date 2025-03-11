package dev.getelements.elements.rt.remote;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Contains the result of the remote invocation.
 */
public class InvocationResult implements Serializable {

    private Object result;

    public InvocationResult() {}

    public InvocationResult(Object result) {
        setResult(result);
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
     */
    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvocationResult)) return false;

        InvocationResult that = (InvocationResult) o;

        return getResult() != null ? getResult().equals(that.getResult()) : that.getResult() == null;
    }

    @Override
    public int hashCode() {
        return getResult() != null ? getResult().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InvocationResult{" +
                "result=" + result +
                '}';
    }

}
