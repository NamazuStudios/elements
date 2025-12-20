package dev.getelements.elements.sdk.model.receipt;

import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "A generic receipt that stores user purchase information.")
public class Receipt {

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

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
