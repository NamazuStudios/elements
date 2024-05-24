package dev.getelements.elements.exception.mission;

import dev.getelements.elements.exception.NotFoundException;

public class MissionNotFoundException extends NotFoundException {

    public MissionNotFoundException() {}

    public MissionNotFoundException(String message) {
        super(message);
    }

    public MissionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissionNotFoundException(Throwable cause) {
        super(cause);
    }

    public MissionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
