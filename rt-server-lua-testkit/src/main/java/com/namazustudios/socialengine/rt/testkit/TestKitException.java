package com.namazustudios.socialengine.rt.testkit;

public class TestKitException extends RuntimeException {

    public TestKitException() {}

    public TestKitException(String message) {
        super(message);
    }

    public TestKitException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestKitException(Throwable cause) {
        super(cause);
    }

    public TestKitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
