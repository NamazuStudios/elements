package com.namazustudios.socialengine.model.googleplayiapreceipt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@ApiModel
public class CreateGooglePlayIapReceipt implements Serializable {
    @ApiModelProperty("The product id purchased by the user, e.g. `com.namazustudios.example_app.pack_10_coins`.")
    @NotNull
    private String productId;

    @ApiModelProperty("The token issued to the user upon successful Google Play purchase transaction.")
    @NotNull
    private String token;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateGooglePlayIapReceipt that = (CreateGooglePlayIapReceipt) o;
        return Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductId(), getToken());
    }

    @Override
    public String toString() {
        return "CreateGooglePlayIapReceipt{" +
                "productId='" + productId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}