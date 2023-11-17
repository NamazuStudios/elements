package dev.getelements.elements.model.index;

import java.util.Objects;

public class IndexPlan<IdentifierT> {

    private IndexPlanStep<IdentifierT> steps;

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
        return Objects.equals(getSteps(), indexPlan.getSteps());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSteps());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IndexPlan{");
        sb.append("steps=").append(steps);
        sb.append('}');
        return sb.toString();
    }

}
