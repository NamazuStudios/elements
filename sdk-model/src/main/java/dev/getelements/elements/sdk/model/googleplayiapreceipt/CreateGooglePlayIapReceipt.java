package dev.getelements.elements.sdk.model.googleplayiapreceipt;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Schema
public class CreateGooglePlayIapReceipt implements Serializable {
    @Schema(description =
            "The package name of the app. Note that this may be different from the " +
            "applicationId/uniqueIdentifier which is why it should be provided with the request.")
    @NotNull
    private String packageName;

    @Schema(description = "The product id purchased by the user, e.g. `com.namazustudios.example_app.pack_10_coins`.")
    @NotNull
    private String productId;

    @Schema(description = "The token issued to the user upon successful Google Play purchase transaction.")
    @NotNull
    private String purchaseToken;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateGooglePlayIapReceipt that = (CreateGooglePlayIapReceipt) o;
        return Objects.equals(getPackageName(), that.getPackageName()) &&
                Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getPurchaseToken(), that.getPurchaseToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPackageName(), getProductId(), getPurchaseToken());
    }

    @Override
    public String toString() {
        return "CreateGooglePlayIapReceipt{" +
                "packageName='" + packageName + '\'' +
                ", productId='" + productId + '\'' +
                ", purchaseToken='" + purchaseToken + '\'' +
                '}';
    }
}