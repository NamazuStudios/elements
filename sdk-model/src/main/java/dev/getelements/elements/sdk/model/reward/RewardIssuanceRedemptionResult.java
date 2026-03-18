package dev.getelements.elements.sdk.model.reward;

import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import io.swagger.v3.oas.annotations.media.Schema;


import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the result of a reward issuance redemption, housing either the resultant
 * {@link RewardIssuance} or the error details.
 */
@Schema(description = "Represents the result of a reward issuance redemption, housing either the resultant " +
        "RewardIssuance or the error details.")
public class RewardIssuanceRedemptionResult implements Serializable {

    /** Creates a new instance. */
    public RewardIssuanceRedemptionResult() {}

    @Schema(description = "The id as originally provided in the request.")
    private String rewardIssuanceId;

    @Schema(description = "Should the redemption be successful, the updated RewardIssuance. Otherwise, null.")
    private RewardIssuance rewardIssuance;

    @Schema(description = "Should the redemption be successful, the Inventory Item that was updated. Otherwise, null.")
    private InventoryItem inventoryItem;

    @Schema(description = "Should the redemption fail, the error details. Otherwise, null.")
    private String errorDetails;

    /**
     * Returns the reward issuance ID as originally provided in the request.
     *
     * @return the reward issuance ID
     */
    public String getRewardIssuanceId() {
        return rewardIssuanceId;
    }

    /**
     * Sets the reward issuance ID as originally provided in the request.
     *
     * @param rewardIssuanceId the reward issuance ID
     */
    public void setRewardIssuanceId(String rewardIssuanceId) {
        this.rewardIssuanceId = rewardIssuanceId;
    }

    /**
     * Returns the updated reward issuance if the redemption was successful, or null otherwise.
     *
     * @return the reward issuance
     */
    public RewardIssuance getRewardIssuance() {
        return rewardIssuance;
    }

    /**
     * Sets the updated reward issuance if the redemption was successful.
     *
     * @param rewardIssuance the reward issuance
     */
    public void setRewardIssuance(RewardIssuance rewardIssuance) {
        this.rewardIssuance = rewardIssuance;
    }

    /**
     * Returns the inventory item that was updated if the redemption was successful, or null otherwise.
     *
     * @return the inventory item
     */
    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    /**
     * Sets the inventory item that was updated if the redemption was successful.
     *
     * @param inventoryItem the inventory item
     */
    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }

    /**
     * Returns the error details if the redemption failed, or null otherwise.
     *
     * @return the error details
     */
    public String getErrorDetails() {
        return errorDetails;
    }

    /**
     * Sets the error details if the redemption failed.
     *
     * @param errorDetails the error details
     */
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
