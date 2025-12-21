package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Schema(
        description = "Houses the various parameters required which allow communication with " +
                "the Oculus API.")
public class OculusApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    @NotNull
    @Schema(description = "The AppID as it appears in the Facebook Developer Console")
    private String applicationId;

    @NotNull
    @Schema(description = "The App Secret as it appears in the Facebook Developer Console")
    private String applicationSecret;

    @Schema(description = "The set of built-in permissions connected clients will need to request.")
    private List<String> builtinApplicationPermissions;

    @Valid
    @Schema(description = "The list of product bundles that may be rewarded upon successful IAP transactions.")
    private List<ProductBundle> productBundles;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    public List<String> getBuiltinApplicationPermissions() {
        return builtinApplicationPermissions;
    }

    public void setBuiltinApplicationPermissions(List<String> builtinApplicationPermissions) {
        this.builtinApplicationPermissions = builtinApplicationPermissions;
    }

    public List<ProductBundle> getProductBundles() {
        return productBundles;
    }

    public void setProductBundles(List<ProductBundle> productBundles) {
        this.productBundles = productBundles;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OculusApplicationConfiguration that)) return false;
        return Objects.equals(applicationId, that.applicationId) && Objects.equals(applicationSecret, that.applicationSecret) && Objects.equals(builtinApplicationPermissions, that.builtinApplicationPermissions) && Objects.equals(productBundles, that.productBundles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, applicationSecret, builtinApplicationPermissions, productBundles);
    }

    @Override
    public String toString() {
        return "OculusApplicationConfiguration{" +
                "applicationId='" + applicationId + '\'' +
                ", applicationSecret='" + applicationSecret + '\'' +
                ", builtinApplicationPermissions=" + builtinApplicationPermissions +
                ", productBundles=" + productBundles +
                '}';
    }
}
