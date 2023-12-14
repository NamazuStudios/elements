package dev.getelements.elements.model.index;

import io.swagger.annotations.ApiModel;

@ApiModel
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
