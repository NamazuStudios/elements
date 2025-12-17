package dev.getelements.elements.sdk.service.meta.oculusiap.client.model;

import dev.getelements.elements.rt.annotation.ClientSerializationStrategy;

import java.util.Objects;

import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.META_GRAPH;

@ClientSerializationStrategy(META_GRAPH)
public class OculusIapVerifyReceiptResponse {

    private boolean success;

    private long grantTime;

    private String userId;

    private String sku;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getGrantTime() {
        return grantTime;
    }

    public void setGrantTime(long grantTime) {
        this.grantTime = grantTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OculusIapVerifyReceiptResponse that)) return false;
        return success == that.success && grantTime == that.grantTime && Objects.equals(userId, that.userId) && Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, grantTime, userId, sku);
    }

    @Override
    public String toString() {
        return "OculusIapVerifyReceiptResponse{" +
                "success=" + success +
                ", grantTime=" + grantTime +
                ", userId='" + userId + '\'' +
                ", sku='" + sku + '\'' +
                '}';
    }
}