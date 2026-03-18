package dev.getelements.elements.sdk.model.index;

/**
 * Represents a single step in an index plan.
 *
 * @param <IdentifierT> the identifier type
 */
public class IndexPlanStep<IdentifierT> {

    /** Creates a new instance. */
    public IndexPlanStep() {}

    private String description;

    private IndexOperation operation;

    private IndexMetadata<IdentifierT> indexMetadata;

    /**
     * A human-readable description of this index step.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this step.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the operation to perform.
     *
     * @return the operation
     */
    public IndexOperation getOperation() {
        return operation;
    }

    /**
     * Sets the operation to perform on this step.
     *
     * @param operation the operation
     */
    public void setOperation(IndexOperation operation) {
        this.operation = operation;
    }

    /**
     * Gets the {@link IndexMetadata} for the index.
     *
     * @return the unique name of the index
     */
    public IndexMetadata<IdentifierT> getIndexMetadata() {
        return indexMetadata;
    }

    /**
     * Sets the {@link IndexMetadata} for the index.
     *
     * @param indexMetadata the index metadata
     */
    public void setIndexMetadata(IndexMetadata indexMetadata) {
        this.indexMetadata = indexMetadata;
    }

}
