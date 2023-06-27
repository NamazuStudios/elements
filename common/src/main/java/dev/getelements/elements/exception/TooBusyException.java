package dev.getelements.elements.exception;

/**
 * Created by patricktwohig on 3/30/15.
 */
public class TooBusyException extends BaseException {

    public TooBusyException() {}

    public TooBusyException(String message) {
        super(message);
    }

    public TooBusyException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooBusyException(Throwable cause) {
        super(cause);
    }

    public TooBusyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.OVERLOAD;
    }

}
