package com.namazustudios.socialengine.exception;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * Created by patricktwohig on 3/25/15.
 */
public abstract class BaseException extends RuntimeException {

    private static final Map<Class<? extends BaseException>, Function<BaseException, Throwable>> tracers = new ConcurrentHashMap<>();

    private Function<BaseException, Throwable> getTracer(final Class<? extends BaseException> cls) {
        return tracers.computeIfAbsent(cls, c -> {
            final var enabled = System.getProperty(format("%s.%s", c, "trace.enabled"), "false");
            return Boolean.parseBoolean(enabled) ? BaseException::forceFillInStackTrace : t -> t;
        });
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
        return getTracer(getClass()).apply(this);
    }

    /**
     * Gets the error code.
     *
     * @return the {@link ErrorCode}
     */
    public abstract ErrorCode getCode();

    /**
     * Forcibly fills in the exception's stack trace, overriding any system flags.
     *
     * @return the stack trace
     */
    protected Throwable forceFillInStackTrace() {
        return super.fillInStackTrace();
    }

}
