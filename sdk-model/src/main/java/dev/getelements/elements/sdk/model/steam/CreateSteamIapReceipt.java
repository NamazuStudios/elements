package dev.getelements.elements.sdk.model.steam;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Objects;

/** Represents the request body for verifying and creating a Steam IAP receipt. */
@Schema
public class CreateSteamIapReceipt implements Serializable {

    /** Creates a new instance. */
    public CreateSteamIapReceipt() {}

    @Schema(description = "The Steam order ID returned by the Steam client upon a successful purchase transaction. " +
            "This is used to query and verify the transaction via the Steam ISteamMicroTxn API.")
    @NotNull
    private String orderId;

    /**
     * Returns the Steam order ID.
     *
     * @return the order ID
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the Steam order ID.
     *
     * @param orderId the order ID
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateSteamIapReceipt that = (CreateSteamIapReceipt) o;
        return Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "CreateSteamIapReceipt{" +
                "orderId='" + orderId + '\'' +
                '}';
    }

}
