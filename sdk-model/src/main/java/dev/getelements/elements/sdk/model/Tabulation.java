package dev.getelements.elements.sdk.model;

import jakarta.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;

/**
 * Similar to a {@link Pagination}, a Tabulation is a way to walk a dataset.
 *
 * @param <ModelT>
 */
public class Tabulation<ModelT> implements Iterable<ModelT> {

    @NotNull
    private List<ModelT> rows;

    @Override
    public Iterator<ModelT> iterator() {
        return getRows().iterator();
    }

    /**
     * Gets the rows of this tabulation.
     * @return
     */
    public List<ModelT> getRows() {
        return rows;
    }

    /**
     * Sets the rows of this tabulation.
     * @param rows
     */
    public void setRows(List<ModelT> rows) {
        this.rows = rows;
    }

}
