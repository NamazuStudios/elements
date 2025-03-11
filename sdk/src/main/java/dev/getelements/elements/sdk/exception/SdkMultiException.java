package dev.getelements.elements.sdk.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.List.copyOf;
import static java.util.List.of;

public class SdkMultiException extends SdkException {

    private final List<Throwable> causes;

    public SdkMultiException() {
        causes = of();
    }

    public SdkMultiException(String message) {
        super(message);
        causes = of();
    }

    public SdkMultiException(final String message, final Collection<? extends  Throwable> causes) {
        super(message, causes.stream().findFirst().orElse(null));
        this.causes = copyOf(causes);
    }

    public SdkMultiException(final Throwable cause) {
        super(cause);
        causes = of(cause);
    }

    public SdkMultiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        causes = of(cause);
    }

    public List<Throwable> getCauses() {
        return causes;
    }

}
