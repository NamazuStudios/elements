package dev.getelements.elements.sdk.model.index;

import java.util.Objects;

public class IndexPlan<IdentifierT> {

    private String id;

    private IndexPlanState state;

    private IndexPlanStep<IdentifierT> steps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IndexPlanState getState() {
        return state;
    }

    public void setState(IndexPlanState state) {
        this.state = state;
    }

    public IndexPlanStep<IdentifierT> getSteps() {
        return steps;
    }

    public void setSteps(IndexPlanStep<IdentifierT> steps) {
        this.steps = steps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexPlan<?> indexPlan = (IndexPlan<?>) o;
        return Objects.equals(getId(), indexPlan.getId()) && getState() == indexPlan.getState() && Objects.equals(getSteps(), indexPlan.getSteps());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getState(), getSteps());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IndexPlan{");
        sb.append("id='").append(id).append('\'');
        sb.append(", state=").append(state);
        sb.append(", steps=").append(steps);
        sb.append('}');
        return sb.toString();
    }

}
