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

}
