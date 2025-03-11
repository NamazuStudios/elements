package dev.getelements.elements.sdk.model.index;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Starts a new indexing operation.")
public class BuildIndexRequest {

    @Schema(description = "Set to true to plan the index building.")
    private boolean plan;

    @Schema(description = "Set to true to perform the index building.")
    private List<IndexableType> toIndex;

    public boolean isPlan() {
        return plan;
    }

    public void setPlan(boolean plan) {
        this.plan = plan;
    }

    public List<IndexableType> getToIndex() {
        return toIndex;
    }

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
