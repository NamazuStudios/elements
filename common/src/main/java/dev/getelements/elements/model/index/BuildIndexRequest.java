package dev.getelements.elements.model.index;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(description = "Starts a new indexing operation.")
public class BuildIndexRequest {

    @ApiModelProperty("Set to true to plan the index building.")
    private boolean plan;

    @ApiModelProperty("Set to true to perform the index building.")
    private boolean buildCustom;

    public boolean isPlan() {
        return plan;
    }

    public void setPlan(boolean plan) {
        this.plan = plan;
    }

    public boolean isBuildCustom() {
        return buildCustom;
    }

    public void setBuildCustom(boolean buildCustom) {
        this.buildCustom = buildCustom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildIndexRequest that = (BuildIndexRequest) o;
        return isPlan() == that.isPlan() && isBuildCustom() == that.isBuildCustom();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPlan(), isBuildCustom());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateIndexRequest{");
        sb.append("plan=").append(plan);
        sb.append(", build=").append(buildCustom);
        sb.append('}');
        return sb.toString();
    }

}
