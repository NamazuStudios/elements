package dev.getelements.elements.rt.util;

/**
 * A simple reference type intended to be used in lambda functions.
 *
 * @param <T>
 */
public class SimpleReference<T> implements Comparable<SimpleReference<T>> {

    private T value;

    public SimpleReference() {
        this(null);
    }

    public SimpleReference(final T value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value.
     */
    public T get() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param t the value
     * @return the value that was just set
     */
    public T set(final T t) {
        return this.value = t;
    }

    /**
     * If the type T is of the {@link Comparable<T>} then, this will perform the comparison. Otherwise, this will result
     * in a {@link ClassCastException}.
     *
     * @param o the object to be compared.
     * @return the comparison value
     */
    @Override
    public int compareTo(final SimpleReference<T> o) {
        final Comparable<T> thisValue = (Comparable<T>) value;
        final Comparable<T> otherValue = (Comparable<T>) o.value;
        return thisValue.compareTo((T)otherValue);
    }

}
