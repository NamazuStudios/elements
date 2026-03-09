package dev.getelements.elements.sdk.model.index;

import java.util.Objects;

/**
 * Represents a plan for index operations.
 *
 * @param <IdentifierT> the identifier type
 */
public class IndexPlan<IdentifierT> {

    /** Creates a new instance. */
    public IndexPlan() {}

    private String id;

    private IndexPlanState state;

    private IndexPlanStep<IdentifierT> steps;

    /**
     * Returns the plan ID.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the plan ID.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the plan state.
     *
     * @return the state
     */
    public IndexPlanState getState() {
        return state;
    }

    /**
     * Sets the plan state.
     *
     * @param state the state
     */
    public void setState(IndexPlanState state) {
        this.state = state;
    }

    /**
     * Returns the plan steps.
     *
     * @return the steps
     */
    public IndexPlanStep<IdentifierT> getSteps() {
        return steps;
    }

    /**
     * Sets the plan steps.
     *
     * @param steps the steps
     */
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
