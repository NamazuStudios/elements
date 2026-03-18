package dev.getelements.elements.sdk.model;

import jakarta.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.List;

/**
 * Similar to a {@link Pagination}, a Tabulation is a way to walk a dataset.
 *
 * @param <ModelT> the row element type
 */
public class Tabulation<ModelT> implements Iterable<ModelT> {

    /** Creates a new instance. */
    public Tabulation() {}

    @NotNull
    private List<ModelT> rows;

    @Override
    public Iterator<ModelT> iterator() {
        return getRows().iterator();
    }

    /**
     * Gets the rows of this tabulation.
     * @return the rows
     */
    public List<ModelT> getRows() {
        return rows;
    }

    /**
     * Sets the rows of this tabulation.
     * @param rows the rows
     */
    public void setRows(List<ModelT> rows) {
        this.rows = rows;
    }

}
