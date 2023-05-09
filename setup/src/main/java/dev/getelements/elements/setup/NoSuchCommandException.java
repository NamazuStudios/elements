package dev.getelements.elements.setup;

public class NoSuchCommandException extends IllegalArgumentException {

    public NoSuchCommandException() {}

    public NoSuchCommandException(String s) {
        super(s);
    }

    public NoSuchCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchCommandException(Throwable cause) {
        super(cause);
    }

}
