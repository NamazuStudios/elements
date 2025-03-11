package dev.getelements.elements.sdk.model.index;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public enum IndexPlanState {

    /**
     * Indicates the plan is ready to be applied.
     */
    READY,

    /**
     * Indicates that the plan execution is in-progress.
     */
    PROCESSING,

    /**
     * Indicates that the operation was successful and the plan fully applied.
     */
    APPLIED

}
