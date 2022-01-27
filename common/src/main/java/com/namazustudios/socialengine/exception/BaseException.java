package com.namazustudios.socialengine.exception;

import java.util.function.Function;

import static java.lang.String.format;

/**
 * Created by patricktwohig on 3/25/15.
 */
public abstract class BaseException extends RuntimeException {

    private static final Function<BaseException, Throwable> tracer;

    static {

        final var enabled = Boolean.parseBoolean(System.getProperty(
            format("%s.%s", BaseException.class.getName(), "trace.enabled"),
            "false")
        );

        tracer = enabled ? BaseException::doFillInStackTrace : t -> t;

    }

    public BaseException() {}

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return tracer.apply(this);
    }

    /**
     * Gets the error code.
     *
     * @return the {@link ErrorCode}
     */
    public abstract ErrorCode getCode();

    private Throwable doFillInStackTrace() {
        return super.fillInStackTrace();
    }

}
