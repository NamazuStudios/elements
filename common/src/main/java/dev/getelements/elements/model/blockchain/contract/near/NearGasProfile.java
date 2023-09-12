package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModelProperty;

public class NearGasProfile {
    @ApiModelProperty("cost_category")
    private String costCategory;

    @ApiModelProperty("cost")
    private NearCostType cost;

    @ApiModelProperty("gas_used")
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
