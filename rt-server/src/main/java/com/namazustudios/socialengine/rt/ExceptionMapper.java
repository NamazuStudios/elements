package com.namazustudios.socialengine.rt;

/**
 * Generates an instance of {@link Response} as the result
 * of any user code throwing an exception.  If no exception
 * handler is available to catch the exception, then the
 * server container handles the exception.
 *
 *
 * Created by patricktwohig on 7/29/15.
 */
public interface ExceptionMapper<ExceptionT extends Exception> {

    /**
     * Maps the given exception to a custom {@link Response}
     *
     * @param exception
     * @return
     */
    Response<?> map(final ExceptionT exception);

    interface Resolver {

        <ExceptionT extends Exception>
        ExceptionMapper<Exception> getExceptionMapper(ExceptionT ex);

    }

}
