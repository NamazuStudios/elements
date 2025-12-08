package dev.getelements.elements.sdk.service.facebookiap.client.model;

import dev.getelements.elements.rt.annotation.ClientSerializationStrategy;

import java.util.Objects;

import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.OCULUS_GRAPH;

@ClientSerializationStrategy(OCULUS_GRAPH)
public class FacebookIapConsumeResponse {

    private boolean success;

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacebookIapConsumeResponse that = (FacebookIapConsumeResponse) o;
        return Objects.equals(getSuccess(), that.getSuccess());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSuccess());
    }

    @Override
    public String toString() {
        return "FacebookIapConsumeResponse{" +
                "success=" + success +
                '}';
    }
}