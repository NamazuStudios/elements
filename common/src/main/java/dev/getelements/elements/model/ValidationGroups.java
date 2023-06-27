package dev.getelements.elements.model;

/**
 * Used in conjunction with the javax.validation annotations to specify the various groups in annotation.  This is by
 * no means intended to be an exhaustive listing of various use-cases for validation.  Rather, this is intended to
 * house the most generic and commonly used.
 */
public interface ValidationGroups {

    /**
     * Used in context of creating the object, such as when used in a POST request.
     */
    @Deprecated
    interface Create {}

    /**
     * Used in the context of updating an object, such as when used ina  PUT request.
     */
    interface Update {}

    /**
     * Used in the context of inserting the object in the database for the first time.  Similar to, but differs from a
     * {@link Create} in that the database layer may impose additional restrictions before actually performing the
     * insert.
     */
    interface Insert {}

    /**
     * Used when the value is read from the database.
     */
    interface Read {}

}
