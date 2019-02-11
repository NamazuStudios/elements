package com.namazustudios.socialengine.model.googleplayiapreceipt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@ApiModel
public class CreateGooglePlayIapReceipt implements Serializable {
    @ApiModelProperty("The package name of the app. Note that this may be different from the " +
            "applicationId/uniqueIdentifier which is why it should be provided with the request.")
    @NotNull
    private String packageName;

    @ApiModelProperty("The product id purchased by the user, e.g. `com.namazustudios.example_app.pack_10_coins`.")
    @NotNull
    private String productId;

    @ApiModelProperty("The token issued to the user upon successful Google Play purchase transaction.")
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