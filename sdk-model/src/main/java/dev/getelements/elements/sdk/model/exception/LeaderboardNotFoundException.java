package dev.getelements.elements.sdk.model.exception;

/** Thrown when a leaderboard cannot be found. */
public class LeaderboardNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public LeaderboardNotFoundException() {
    }

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public LeaderboardNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public LeaderboardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public LeaderboardNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public LeaderboardNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
