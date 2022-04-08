package com.namazustudios.socialengine.rt.exception;

public class UncheckedInterruptedException extends InternalException {

    public UncheckedInterruptedException(final InterruptedException cause) {
        super(cause);
    }

    @Override
    public synchronized InterruptedException getCause() {
        return (InterruptedException) super.getCause();
    }

}
