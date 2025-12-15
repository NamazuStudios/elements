package dev.getelements.elements.sdk.model.receipt;

import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "A request model to create a new receipt.")
public class CreateReceiptRequest {

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

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getBody() {
        return body;
    }

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