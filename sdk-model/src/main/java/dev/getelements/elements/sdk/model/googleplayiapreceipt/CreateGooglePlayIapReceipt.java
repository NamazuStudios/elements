package dev.getelements.elements.sdk.model.googleplayiapreceipt;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/** Represents the request body for creating a Google Play IAP receipt. */
@Schema
public class CreateGooglePlayIapReceipt implements Serializable {

    /** Creates a new instance. */
    public CreateGooglePlayIapReceipt() {}
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

    /**
     * Returns the package name of the app.
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package name of the app.
     *
     * @param packageName the package name
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Returns the product ID purchased by the user.
     *
     * @return the product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the product ID purchased by the user.
     *
     * @param productId the product ID
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the purchase token issued upon successful purchase.
     *
     * @return the purchase token
     */
    public String getPurchaseToken() {
        return purchaseToken;
    }

    /**
     * Sets the purchase token issued upon successful purchase.
     *
     * @param purchaseToken the purchase token
     */
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