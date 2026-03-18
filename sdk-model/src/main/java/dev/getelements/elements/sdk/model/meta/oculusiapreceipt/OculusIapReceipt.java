package dev.getelements.elements.sdk.model.meta.oculusiapreceipt;

import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Represents an IAP receipt from the Oculus platform. */
@Schema
public class OculusIapReceipt implements Serializable {

    /** Creates a new instance. */
    public OculusIapReceipt() {}

    /** The prefix used for identifying reward issuance tags. */
    public static final String ID_TAG_PREFIX = "ID";

    /** The separator used in reward issuance tag construction. */
    public static final String TAG_SEPARATOR = ".";

    @Schema(description = "The id of the Oculus User that purchased the IAP.")
    private String userId;

    @Schema(description =
            "The original transaction identifier of the IAP. We use this as the key for the db object " +
            "as well as the {@link RewardIssuance} context. (For now, we do not persist the transaction_id, only " +
            "the original_transaction_id.)")
    @NotNull(groups={Create.class, Insert.class})
    private String purchaseId;

    @Schema(description = "A secondary identifier for troubleshooting/auditing (and potential future correlation)")
    private String reportingId;

    @Schema(description =
            "The SKU of the IAP as it is listed in your IAP definitions.")
    @NotNull(groups={Create.class, Insert.class})
    private String sku;

    @Schema(description = "Time when the user gained entitlement to the item (Unix timestamp).")
    @NotNull(groups={Create.class, Insert.class})
    private long grantTime;

    @Schema(description = "Time when the user will lose entitlement to the item (Unix timestamp). If the user has an indefinite entitlement it will be 0.")
    @NotNull(groups={Create.class, Insert.class})
    private long expirationTime;

    @Schema(description = "The original purchase date.")
    private String developerPayload;

    /**
     * Returns the Oculus user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the Oculus user ID.
     *
     * @param fbUserId the user ID
     */
    public void setUserId(String fbUserId) {
        this.userId = fbUserId;
    }

    /**
     * Returns the original transaction identifier (purchase ID).
     *
     * @return the purchase ID
     */
    public String getPurchaseId() {
        return purchaseId;
    }

    /**
     * Sets the original transaction identifier (purchase ID).
     *
     * @param purchaseId the purchase ID
     */
    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    /**
     * Returns the reporting ID for troubleshooting and auditing.
     *
     * @return the reporting ID
     */
    public String getReportingId() {
        return reportingId;
    }

    /**
     * Sets the reporting ID for troubleshooting and auditing.
     *
     * @param reportingId the reporting ID
     */
    public void setReportingId(String reportingId) {
        this.reportingId = reportingId;
    }

    /**
     * Returns the SKU of the IAP item.
     *
     * @return the SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Sets the SKU of the IAP item.
     *
     * @param sku the SKU
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Returns the time when the user gained entitlement (Unix timestamp).
     *
     * @return the grant time
     */
    public long getGrantTime() {
        return grantTime;
    }

    /**
     * Sets the time when the user gained entitlement (Unix timestamp).
     *
     * @param grantTime the grant time
     */
    public void setGrantTime(long grantTime) {
        this.grantTime = grantTime;
    }

    /**
     * Returns the time when the user will lose entitlement (Unix timestamp).
     *
     * @return the expiration time
     */
    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the time when the user will lose entitlement (Unix timestamp).
     *
     * @param expirationTime the expiration time
     */
    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Returns the developer payload.
     *
     * @return the developer payload
     */
    public String getDeveloperPayload() {
        return developerPayload;
    }

    /**
     * Sets the developer payload.
     *
     * @param developerPayload the developer payload
     */
    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    /**
     * Builds the reward issuance tags for the given original transaction ID.
     *
     * @param originalTransactionId the original transaction ID
     * @return the list of reward issuance tags
     */
    public static List<String> buildRewardIssuanceTags(final String originalTransactionId) {
        final List <String> tags = new ArrayList<>();
        tags.add(buildIdentifyingRewardIssuanceTag(originalTransactionId));

        return tags;
    }

    /**
     * Builds the identifying reward issuance tag for the given original transaction ID.
     *
     * @param originalTransactionId the original transaction ID
     * @return the identifying reward issuance tag
     */
    public static String buildIdentifyingRewardIssuanceTag(final String originalTransactionId) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ID_TAG_PREFIX);
        stringBuilder.append(TAG_SEPARATOR);
        stringBuilder.append(originalTransactionId);

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OculusIapReceipt that = (OculusIapReceipt) o;
        return Objects.equals(getSku(), that.getSku()) &&
                Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getPurchaseId(), that.getPurchaseId()) &&
                Objects.equals(getReportingId(), that.getReportingId()) &&
                Objects.equals(getExpirationTime(), that.getExpirationTime()) &&
                Objects.equals(getDeveloperPayload(), that.getDeveloperPayload());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getPurchaseId(), getSku(), getGrantTime(), getExpirationTime(), getReportingId(), getDeveloperPayload());
    }

    @Override
    public String toString() {
        return "OculusIapReceipt{" +
                "userId=" + userId +
                ", purchaseId='" + purchaseId + '\'' +
                ", reportingId='" + reportingId + '\'' +
                ", sku='" + sku + '\'' +
                ", grantTime=" + grantTime +
                ", expirationTime=" + expirationTime +
                ", developerPayload='" + developerPayload + '\'' +
                '}';
    }
}