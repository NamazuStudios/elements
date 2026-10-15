package dev.getelements.elements.sdk.model.exception;

/**
 * Thrown when a caller has exceeded a configured rate limit. Maps to HTTP {@code 429 Too Many Requests}.
 */
public class RateLimitedException extends BaseException {

    /** Creates a new instance. */
    public RateLimitedException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public RateLimitedException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public RateLimitedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public RateLimitedException(Throwable cause) {
        super(cause);
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.RATE_LIMITED;
    }

}
