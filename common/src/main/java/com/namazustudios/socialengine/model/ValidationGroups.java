package com.namazustudios.socialengine.model;

/**
 * Used in conjection with the javax.validation annotations to specify the various groups in annotation.  This is by
 * no means intended to be an exhaustive listing of various use-cases for validation.  Rather, this is intended to
 * house the most generic and commonly used.
 */
public interface ValidationGroups {

    /**
     * Used in context of creating the object.
     */
    interface Create {}

    /**
     * Used in context of updating the object.
     */
    interface Update {}

}
