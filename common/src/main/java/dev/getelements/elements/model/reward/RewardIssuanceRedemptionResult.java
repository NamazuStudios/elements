package dev.getelements.elements.model.reward;

import dev.getelements.elements.model.inventory.InventoryItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Objects;

@ApiModel(description = "Represents the result of a reward issuance redemption, housing either the resultant " +
        "RewardIssuance or the error details.")
public class RewardIssuanceRedemptionResult implements Serializable {
    @ApiModelProperty("The id as originally provided in the request.")
    private String rewardIssuanceId;

    @ApiModelProperty("Should the redemption be successful, the updated RewardIssuance. Otherwise, null.")
    private RewardIssuance rewardIssuance;

    @ApiModelProperty("Should the redemption be successful, the Inventory Item that was updated. Otherwise, null.")
    private InventoryItem inventoryItem;

    @ApiModelProperty("Should the redemption fail, the error details. Otherwise, null.")
    private String errorDetails;

    public String getRewardIssuanceId() {
        return rewardIssuanceId;
    }

    public void setRewardIssuanceId(String rewardIssuanceId) {
        this.rewardIssuanceId = rewardIssuanceId;
    }

    public RewardIssuance getRewardIssuance() {
        return rewardIssuance;
    }

    public void setRewardIssuance(RewardIssuance rewardIssuance) {
        this.rewardIssuance = rewardIssuance;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewardIssuanceRedemptionResult that = (RewardIssuanceRedemptionResult) o;
        return Objects.equals(getRewardIssuanceId(), that.getRewardIssuanceId()) &&
                Objects.equals(getRewardIssuance(), that.getRewardIssuance()) &&
                Objects.equals(getInventoryItem(), that.getInventoryItem()) &&
                Objects.equals(getErrorDetails(), that.getErrorDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRewardIssuanceId(), getRewardIssuance(), getInventoryItem(), getErrorDetails());
    }

    @Override
    public String toString() {
        return "RewardIssuanceRedemptionResult{" +
                "rewardIssuanceId='" + rewardIssuanceId + '\'' +
                ", rewardIssuance=" + rewardIssuance +
                ", inventoryItem=" + inventoryItem +
                ", errorDetails='" + errorDetails + '\'' +
                '}';
    }
}
