package dev.getelements.elements.sdk.model.receipt;

import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/** A generic receipt that stores user purchase information. */
@Schema(description = "A generic receipt that stores user purchase information.")
public class Receipt {

    /** Creates a new instance. */
    public Receipt() {}

    @Schema(description = "The db id of this receipt.")
    private String id;

    @Schema(description = "The id of the original transaction as provided by the payment processor.")
    private String originalTransactionId;

    @Schema(description = "The id of the receipt provider in reverse-dns notation, e.g. com.company.platform")
    private String schema;

    @Schema(description = "The User associated with this receipt.")
    private User user;

    @Schema(description = "The time that the purchase was made (ms since Unix epoch).")
    private long purchaseTime;

    @Schema(description = "The string representation of the raw receipt data.")
    private String body;

    /**
     * Returns the database ID of this receipt.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the database ID of this receipt.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the original transaction ID as provided by the payment processor.
     *
     * @return the original transaction ID
     */
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    /**
     * Sets the original transaction ID as provided by the payment processor.
     *
     * @param originalTransactionId the original transaction ID
     */
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    /**
     * Returns the receipt provider schema in reverse-dns notation.
     *
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the receipt provider schema in reverse-dns notation.
     *
     * @param schema the schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Returns the time that the purchase was made, in milliseconds since Unix epoch.
     *
     * @return the purchase time
     */
    public long getPurchaseTime() {
        return purchaseTime;
    }

    /**
     * Sets the time that the purchase was made, in milliseconds since Unix epoch.
     *
     * @param purchaseTime the purchase time
     */
    public void setPurchaseTime(long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    /**
     * Returns the user associated with this receipt.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user associated with this receipt.
     *
     * @param user the user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the raw receipt data as a string.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the raw receipt data as a string.
     *
     * @param body the body
     */
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Receipt receipt)) return false;
        return purchaseTime == receipt.purchaseTime && Objects.equals(id, receipt.id) && Objects.equals(originalTransactionId, receipt.originalTransactionId) && Objects.equals(schema, receipt.schema) && Objects.equals(user, receipt.user) && Objects.equals(body, receipt.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, originalTransactionId, schema, user, purchaseTime, body);
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "id='" + id + '\'' +
                ", originalTransactionId='" + originalTransactionId + '\'' +
                ", schema='" + schema + '\'' +
                ", user=" + user +
                ", purchaseTime=" + purchaseTime +
                ", body='" + body + '\'' +
                '}';
    }
}
