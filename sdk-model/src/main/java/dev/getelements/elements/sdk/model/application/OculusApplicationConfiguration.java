package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/** Houses the various parameters required which allow communication with the Oculus API. */
@Schema(
        description = "Houses the various parameters required which allow communication with " +
                "the Oculus API.")
public class OculusApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    /** Creates a new instance. */
    public OculusApplicationConfiguration() {}

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
    @Deprecated
    private List<ProductBundle> productBundles;

    /**
     * Returns the application ID from the Facebook Developer Console.
     *
     * @return the application ID
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID from the Facebook Developer Console.
     *
     * @param applicationId the application ID
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the application secret from the Facebook Developer Console.
     *
     * @return the application secret
     */
    public String getApplicationSecret() {
        return applicationSecret;
    }

    /**
     * Sets the application secret from the Facebook Developer Console.
     *
     * @param applicationSecret the application secret
     */
    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    /**
     * Returns the set of built-in permissions connected clients will need to request.
     *
     * @return the builtin application permissions
     */
    public List<String> getBuiltinApplicationPermissions() {
        return builtinApplicationPermissions;
    }

    /**
     * Sets the set of built-in permissions connected clients will need to request.
     *
     * @param builtinApplicationPermissions the builtin application permissions
     */
    public void setBuiltinApplicationPermissions(List<String> builtinApplicationPermissions) {
        this.builtinApplicationPermissions = builtinApplicationPermissions;
    }

    /**
     * Returns the list of product bundles for IAP transactions.
     *
     * @return the product bundles
     * @deprecated use the new goods API instead
     */
    @Deprecated
    public List<ProductBundle> getProductBundles() {
        return productBundles;
    }

    /**
     * Sets the list of product bundles for IAP transactions.
     *
     * @param productBundles the product bundles
     * @deprecated use the new goods API instead
     */
    @Deprecated
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
