package com.namazustudios.socialengine.model;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel("Represents a delta, or change, in a resource.  This, for example, is used to poll for when an " +
          "object (or set of objects) changes such that the client may update state accordingly.")
public abstract class TimeDelta<IdentifierT, ModelT> implements Serializable {

    @ApiModelProperty("The id of the object chich changed.  This must always be present and must match the snapshot's unique ID.")
    private IdentifierT id;

    @ApiModelProperty(
        "The timeStamp at which the delta occurred.  This is effectively the number of edits since the " +
        "object was created.")
    private long timeStamp;

    @ApiModelProperty("The operation which changed the object.")
    private Operation operation;

    @ApiModelProperty("The snapshot of the object itself as the result of the change.  This may not not be available " +
                      "in all circumstances.  This will always be null if the object has been deleted.")
    private ModelT snapshot;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public IdentifierT getId() {
        return id;
    }

    public void setId(IdentifierT id) {
        this.id = id;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public ModelT getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(ModelT snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeDelta)) return false;

        TimeDelta<?, ?> timeDelta = (TimeDelta<?, ?>) o;

        if (getTimeStamp() != timeDelta.getTimeStamp()) return false;
        if (getId() != null ? !getId().equals(timeDelta.getId()) : timeDelta.getId() != null)
            return false;
        if (getOperation() != timeDelta.getOperation()) return false;
        return getSnapshot() != null ? getSnapshot().equals(timeDelta.getSnapshot()) : timeDelta.getSnapshot() == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (getTimeStamp() ^ (getTimeStamp() >>> 32));
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        result = 31 * result + (getOperation() != null ? getOperation().hashCode() : 0);
        result = 31 * result + (getSnapshot() != null ? getSnapshot().hashCode() : 0);
        return result;
    }

    /**
     * The operation which modified the object.
     */
    @ApiModel
    public enum Operation {

        /**
         * The object was created.
         */
        CREATED,

        /**
         * The object was updated.
         */
        UPDATED,

        /**
         * The object was deleted or removed.
         */
        REMOVED

    }

}
