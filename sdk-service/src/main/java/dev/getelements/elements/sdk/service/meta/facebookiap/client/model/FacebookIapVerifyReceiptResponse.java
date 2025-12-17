package dev.getelements.elements.sdk.service.facebookiap.client.model;

import dev.getelements.elements.rt.annotation.ClientSerializationStrategy;

import java.util.Objects;

import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.OCULUS_GRAPH;

@ClientSerializationStrategy(OCULUS_GRAPH)
public class FacebookIapVerifyReceiptResponse {

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
        FacebookIapVerifyReceiptResponse that = (FacebookIapVerifyReceiptResponse) o;
        return Objects.equals(getSuccess(), that.getSuccess()) &&
                Objects.equals(getGrantTime(), that.getGrantTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSuccess(), getGrantTime());
    }

    @Override
    public String toString() {
        return "FacebookIapVerifyReceiptResponse{" +
                "success=" + success +
                ", grantTime=" + grantTime +
                '}';
    }
}