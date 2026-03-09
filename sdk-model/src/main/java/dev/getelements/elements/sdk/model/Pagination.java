package dev.getelements.elements.sdk.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a paginated result set with offset and total count.
 *
 * @param <T> the element type
 */
@Schema
public class Pagination<T> implements Iterable<T>, Serializable {

    /** Creates a new instance. */
    public Pagination() {}

    /**
     * Returns an empty pagination with no results.
     * @param <T> the result type
     * @return an empty pagination
     */
    public static <T> Pagination<T> empty() {
        return new Pagination<>();
    }

    private int offset;

    private int total;

    private boolean approximation;

    private List<T> objects = new ArrayList<T>();

    /**
     * Returns the current page offset.
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the current page offset.
     * @param offset the offset
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns the total number of results.
     * @return the total count
     */
    public int getTotal() {
        return total;
    }

    /**
     * Sets the total number of results.
     * @param total the total count
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * Returns the objects in this page.
     * @return the objects
     */
    public List<T> getObjects() {
        return objects;
    }

    /**
     * Sets the objects in this page.
     * @param objects the objects
     */
    public void setObjects(List<T> objects) {
        this.objects = objects;
    }

    /**
     * Returns whether the total is an approximation.
     * @return true if the total is an approximation
     */
    public boolean isApproximation() {
        return approximation;
    }

    /**
     * Sets whether the total is an approximation.
     * @param approximation true if the total is an approximation
     */
    public void setApproximation(boolean approximation) {
        this.approximation = approximation;
    }

    /**
     * Creates a Pagination from a stream, collecting all elements.
     * @param <U> the element type
     * @param uStream the stream of elements
     * @return a Pagination containing all stream elements
     */
    public static <U> Pagination<U> from(final Stream<U> uStream) {
        final Pagination<U> uPagination = new Pagination<>();
        final List<U> objects = uStream.collect(Collectors.toList());
        uPagination.setObjects(objects);
        uPagination.setTotal(objects.size());
        return uPagination;
    }

    /**
     * Transforms this pagination by applying a function to each element.
     * @param <U> the target element type
     * @param function the transformation function
     * @return a new Pagination with transformed elements
     */
    public <U> Pagination<U> transform(final Function<T, U> function) {

        final Pagination<U> tPagination = new Pagination<>();
        tPagination.setTotal(getTotal());
        tPagination.setOffset(getOffset());
        tPagination.setApproximation(isApproximation());

        if (getObjects() != null) {
            tPagination.setObjects(getObjects()
                .stream()
                .map(function)
                .collect(Collectors.toList()));
        }

        return tPagination;

    }

    /**
     * Returns a sequential stream of the objects in this page.
     * @return a stream of objects
     */
    public Stream<T> stream() {
        return getObjects() == null
                ? Stream.empty()
                : getObjects().stream();
    }

    @Override
    public Iterator<T> iterator() {
        return getObjects().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pagination)) return false;

        Pagination<?> that = (Pagination<?>) o;

        if (getOffset() != that.getOffset()) return false;
        if (getTotal() != that.getTotal()) return false;
        if (isApproximation() != that.isApproximation()) return false;
        return getObjects() != null ? getObjects().equals(that.getObjects()) : that.getObjects() == null;
    }

    @Override
    public int hashCode() {
        int result = getOffset();
        result = 31 * result + getTotal();
        result = 31 * result + (isApproximation() ? 1 : 0);
        result = 31 * result + (getObjects() != null ? getObjects().hashCode() : 0);
        return result;
    }

}
