package dev.getelements.elements.sdk.model.largeobject;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The large object state. A Large Object can exist without contents. This state indicates what contents exist in the
 * object.
 */
@Schema(description = "Represents the state of the large object.")
public enum LargeObjectState {

    /**
     * The large object is empty and has no contents.
     */
    EMPTY,

    /**
     * The large object has been uploaded and currently has contents.
     */
    UPLOADED

}
