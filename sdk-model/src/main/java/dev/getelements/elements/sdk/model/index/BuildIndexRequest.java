package dev.getelements.elements.sdk.model.index;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

/** Starts a new indexing operation. */
@Schema(description = "Starts a new indexing operation.")
public class BuildIndexRequest {

    /** Creates a new instance. */
    public BuildIndexRequest() {}

    @Schema(description = "Set to true to plan the index building.")
    private boolean plan;

    @Schema(description = "Set to true to perform the index building.")
    private List<IndexableType> toIndex;

    /**
     * Returns whether this is a planning run only.
     *
     * @return true if this is a plan-only run
     */
    public boolean isPlan() {
        return plan;
    }

    /**
     * Sets whether this is a planning run only.
     *
     * @param plan true if this is a plan-only run
     */
    public void setPlan(boolean plan) {
        this.plan = plan;
    }

    /**
     * Returns the list of types to index.
     *
     * @return the list of indexable types
     */
    public List<IndexableType> getToIndex() {
        return toIndex;
    }

    /**
     * Sets the list of types to index.
     *
     * @param toIndex the list of indexable types
     */
    public void setToIndex(List<IndexableType> toIndex) {
        this.toIndex = toIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildIndexRequest that = (BuildIndexRequest) o;
        return isPlan() == that.isPlan() && Objects.equals(getToIndex(), that.getToIndex());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPlan(), getToIndex());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BuildIndexRequest{");
        sb.append("plan=").append(plan);
        sb.append(", toIndex=").append(toIndex);
        sb.append('}');
        return sb.toString();
    }

}
