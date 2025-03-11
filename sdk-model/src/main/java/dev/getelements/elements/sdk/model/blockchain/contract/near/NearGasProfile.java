package dev.getelements.elements.sdk.model.blockchain.contract.near;


import io.swagger.v3.oas.annotations.media.Schema;

public class NearGasProfile {
    @Schema(description = "cost_category")
    private String costCategory;

    @Schema(description = "cost")
    private NearCostType cost;

    @Schema(description = "gas_used")
    private String gasUsed;

    public String getCostCategory() {
        return costCategory;
    }

    public void setCostCategory(String costCategory) {
        this.costCategory = costCategory;
    }

    public NearCostType getCost() {
        return cost;
    }

    public void setCost(NearCostType cost) {
        this.cost = cost;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }
}
