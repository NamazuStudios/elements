package com.namazustudios.socialengine.model;

import io.swagger.annotations.ApiModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 3/25/15.
 */
@ApiModel
public class Pagination<T> {

    private int offset;

    private int total;

    private boolean approximation;

    private List<T> objects = new ArrayList<T>();

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getObjects() {
        return objects;
    }

    public void setObjects(List<T> objects) {
        this.objects = objects;
    }

    public boolean isApproximation() {
        return approximation;
    }

    public void setApproximation(boolean approximation) {
        this.approximation = approximation;
    }

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
