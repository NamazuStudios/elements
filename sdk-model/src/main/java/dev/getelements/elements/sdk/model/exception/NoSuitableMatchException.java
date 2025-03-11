package dev.getelements.elements.sdk.model.exception;

import dev.getelements.elements.sdk.model.match.Match;

/**
 * Thrown to indicate there is no suitable {@link Match} found.
 *
 * Created by patricktwohig on 7/27/17.
 */
public class NoSuitableMatchException extends RuntimeException {

    public NoSuitableMatchException() {}

    public NoSuitableMatchException(String message) {
        super(message);
    }

    public NoSuitableMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuitableMatchException(Throwable cause) {
        super(cause);
    }

    public NoSuitableMatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
