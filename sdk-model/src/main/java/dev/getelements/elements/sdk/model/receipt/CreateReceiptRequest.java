package dev.getelements.elements.sdk.model.receipt;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/** A request model to create a new receipt. */
@Schema(description = "A request model to create a new receipt.")
public class CreateReceiptRequest {

    /** Creates a new instance. */
    public CreateReceiptRequest() {}

    @Schema(description = "The id of the original transaction as provided by the payment processor.")
    private String originalTransactionId;

    @Schema(description = "The id of the receipt provider in reverse-dns notation, e.g. com.company.platform")
    private String schema;

    @Schema(description = "The database id, name, or email of the User associated with this receipt.")
    private String userId;

    @Schema(description = "The time that the purchase was made (ms since Unix epoch).")
    private long purchaseTime;

    @Schema(description = "The string representation of the raw receipt data.")
    private String body;

    /**
     * Returns the original transaction ID from the payment processor.
     *
     * @return the original transaction ID
     */
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    /**
     * Sets the original transaction ID from the payment processor.
     *
     * @param originalTransactionId the original transaction ID
     */
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    /**
     * Returns the receipt provider schema.
     *
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the receipt provider schema.
     *
     * @param schema the schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Returns the user ID associated with this receipt.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID associated with this receipt.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the purchase time in milliseconds since Unix epoch.
     *
     * @return the purchase time
     */
    public long getPurchaseTime() {
        return purchaseTime;
    }

    /**
     * Sets the purchase time in milliseconds since Unix epoch.
     *
     * @param purchaseTime the purchase time
     */
    public void setPurchaseTime(long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    /**
     * Returns the raw receipt data body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the raw receipt data body.
     *
     * @param body the body
     */
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateReceiptRequest that)) return false;
        return purchaseTime == that.purchaseTime && Objects.equals(originalTransactionId, that.originalTransactionId) && Objects.equals(schema, that.schema) && Objects.equals(userId, that.userId) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalTransactionId, schema, userId, purchaseTime, body);
    }

    @Override
    public String toString() {
        return "CreateReceiptRequest{" +
                "originalTransactionId='" + originalTransactionId + '\'' +
                ", schema='" + schema + '\'' +
                ", userId='" + userId + '\'' +
                ", purchaseTime=" + purchaseTime +
                ", body='" + body + '\'' +
                '}';
    }
}