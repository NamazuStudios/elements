package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 *
 * Created by patricktwohig on 6/14/17.
 */
@Schema(
    description = "Houses the various parameters required which allow communication with " +
                  "the Facebook API.")
public class FacebookApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    /** Creates a new instance. */
    public FacebookApplicationConfiguration() {}

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
     * Returns the Facebook application ID.
     *
     * @return the application ID
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the Facebook application ID.
     *
     * @param applicationId the application ID
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the Facebook application secret.
     *
     * @return the application secret
     */
    public String getApplicationSecret() {
        return applicationSecret;
    }

    /**
     * Sets the Facebook application secret.
     *
     * @param applicationSecret the application secret
     */
    public void setApplicationSecret(String applicationSecret) {
        this.applicationSecret = applicationSecret;
    }

    /**
     * Returns the built-in application permissions.
     *
     * @return the builtin application permissions
     */
    public List<String> getBuiltinApplicationPermissions() {
        return builtinApplicationPermissions;
    }

    /**
     * Returns the product bundles.
     *
     * @return the product bundles
     * @deprecated use IAP SKU directly
     */
    @Deprecated
    public List<ProductBundle> getProductBundles() {
        return productBundles;
    }

    /**
     * Sets the product bundles.
     *
     * @param productBundles the product bundles
     * @deprecated use IAP SKU directly
     */
    @Deprecated
    public void setProductBundles(List<ProductBundle> productBundles) {
        this.productBundles = productBundles;
    }

    /**
     * Sets the built-in application permissions.
     *
     * @param builtinApplicationPermissions the builtin application permissions
     */
    public void setBuiltinApplicationPermissions(List<String> builtinApplicationPermissions) {
        this.builtinApplicationPermissions = builtinApplicationPermissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FacebookApplicationConfiguration that)) return false;
        if (!super.equals(o)) return false;

        if (getApplicationId() != null ? !getApplicationId().equals(that.getApplicationId()) : that.getApplicationId() != null)
            return false;
        if (getApplicationSecret() != null ? !getApplicationSecret().equals(that.getApplicationSecret()) : that.getApplicationSecret() != null)
            return false;
        if(getBuiltinApplicationPermissions() != null ? getBuiltinApplicationPermissions().equals(that.getBuiltinApplicationPermissions()) : that.getBuiltinApplicationPermissions() != null)
            return false;
        return getProductBundles() != null ? getProductBundles().equals(that.getProductBundles()) : that.getProductBundles() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getApplicationId() != null ? getApplicationId().hashCode() : 0);
        result = 31 * result + (getApplicationSecret() != null ? getApplicationSecret().hashCode() : 0);
        result = 31 * result + (getBuiltinApplicationPermissions() != null ? getBuiltinApplicationPermissions().hashCode() : 0);
        result = 31 * result + (getProductBundles() != null ? getProductBundles().hashCode() : 0);
        return result;
    }

}
