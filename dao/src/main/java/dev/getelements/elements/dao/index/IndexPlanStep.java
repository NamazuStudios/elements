package dev.getelements.elements.dao.index;

import dev.getelements.elements.model.schema.template.TemplateTabField;

public class IndexPlanStep<IdentifierT> {

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

    public void setIndexMetadata(IndexMetadata indexMetadata) {
        this.indexMetadata = indexMetadata;
    }

}
