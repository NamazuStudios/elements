package dev.getelements.elements.rt.exception;

/**
 * Wrap the java standard {@link UncheckedInterruptedException}.
 */
public class UncheckedInterruptedException extends InternalException {

    /**
     * Constructs the {@link UncheckedInterruptedException} from an {@link InterruptedException}, setting the underlying
     * cause to the {@link InterruptedException}
     *
     * @param cause
     */
    public UncheckedInterruptedException(final InterruptedException cause) {
        super(cause.getMessage(), cause);
    }

    @Override
    public synchronized InterruptedException getCause() {
        return (InterruptedException) super.getCause();
    }

}
