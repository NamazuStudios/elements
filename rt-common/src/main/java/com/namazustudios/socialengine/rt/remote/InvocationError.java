package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.Method;

/**
 * Returned when there exists an error.
 */
public class InvocationError {

    private Throwable throwable;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvocationError)) return false;

        InvocationError that = (InvocationError) o;

        return getThrowable() != null ? getThrowable().equals(that.getThrowable()) : that.getThrowable() == null;
    }

    @Override
    public int hashCode() {
        return getThrowable() != null ? getThrowable().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "InvocationError{" +
                "throwable=" + throwable +
                '}';
    }

}
