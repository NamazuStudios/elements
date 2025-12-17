package dev.getelements.elements.sdk.service.meta.oculusiap.client.model;

import dev.getelements.elements.rt.annotation.ClientSerializationStrategy;

import java.util.Objects;

import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.META_GRAPH;

@ClientSerializationStrategy(META_GRAPH)
public class OculusIapVerifyReceiptResponse {

    private boolean success;

    private long grantTime;

    public boolean getSuccess() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OculusIapVerifyReceiptResponse that = (OculusIapVerifyReceiptResponse) o;
        return Objects.equals(getSuccess(), that.getSuccess()) &&
                Objects.equals(getGrantTime(), that.getGrantTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSuccess(), getGrantTime());
    }

    @Override
    public String toString() {
        return "OculusIapVerifyReceiptResponse{" +
                "success=" + success +
                ", grantTime=" + grantTime +
                '}';
    }
}