package dev.getelements.elements.dao.index;

/**
 * Indicates the operation to be performed against the index.
 */
public enum IndexOperation {

    /**
     * Indicates the step should create the indexes.
     */
    CREATE,

    /**
     * Leave the index as-is.
     */
    LEAVE_AS_IS,

    /**
     * Replace the index.
     */
    REPLACE,

    /**
     * Delete the index.
     */
    DELETE

}
