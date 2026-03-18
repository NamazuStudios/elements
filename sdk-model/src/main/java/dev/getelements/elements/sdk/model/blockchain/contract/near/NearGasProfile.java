package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

/** Represents a gas usage profile entry for a NEAR protocol transaction. */
public class NearGasProfile {

    /** Creates a new instance. */
    public NearGasProfile() {}

    @Schema(description = "cost_category")
    private String costCategory;

    @Schema(description = "cost")
    private NearCostType cost;

    @Schema(description = "gas_used")
    private String gasUsed;

    /**
     * Returns the cost category.
     *
     * @return the cost category
     */
    public String getCostCategory() {
        return costCategory;
    }

    /**
     * Sets the cost category.
     *
     * @param costCategory the cost category
     */
    public void setCostCategory(String costCategory) {
        this.costCategory = costCategory;
    }

    /**
     * Returns the cost type.
     *
     * @return the cost type
     */
    public NearCostType getCost() {
        return cost;
    }

    /**
     * Sets the cost type.
     *
     * @param cost the cost type
     */
    public void setCost(NearCostType cost) {
        this.cost = cost;
    }

    /**
     * Returns the amount of gas used.
     *
     * @return the gas used
     */
    public String getGasUsed() {
        return gasUsed;
    }

    /**
     * Sets the amount of gas used.
     *
     * @param gasUsed the gas used
     */
    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }
}
