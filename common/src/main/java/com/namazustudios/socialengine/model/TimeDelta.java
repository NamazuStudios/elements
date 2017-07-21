package com.namazustudios.socialengine.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Represents a delta, or change, in a resource.  This, for example, is used to poll for when an " +
          "object (or set of objects) changes such that the client may update state accordingly.")
public class TimeDelta<IdentifierT, ModelT> {

    @ApiModelProperty(
        "The timeStamp at which the delta occurred.  This is effectively the number of edits since the " +
        "object was created.")
    private long timeStamp;

    @ApiModelProperty("The identifier of the object.  This must always be present.")
    private IdentifierT identifier;

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

    public IdentifierT getIdentifier() {
        return identifier;
    }

    public void setIdentifier(IdentifierT identifier) {
        this.identifier = identifier;
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
        if (getIdentifier() != null ? !getIdentifier().equals(timeDelta.getIdentifier()) : timeDelta.getIdentifier() != null)
            return false;
        if (getOperation() != timeDelta.getOperation()) return false;
        return getSnapshot() != null ? getSnapshot().equals(timeDelta.getSnapshot()) : timeDelta.getSnapshot() == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (getTimeStamp() ^ (getTimeStamp() >>> 32));
        result = 31 * result + (getIdentifier() != null ? getIdentifier().hashCode() : 0);
        result = 31 * result + (getOperation() != null ? getOperation().hashCode() : 0);
        result = 31 * result + (getSnapshot() != null ? getSnapshot().hashCode() : 0);
        return result;
    }

    /**
     * The operation which modified the object.
     */
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
