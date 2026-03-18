package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
public class GooglePlayApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    /** Creates a new instance. */
    public GooglePlayApplicationConfiguration() {}

    private String applicationId;

    private Map<String, Object> jsonKey;

    @Schema(description = "The list of product bundles that may be rewarded upon successful IAP transactions.")
    @Deprecated
    private List<ProductBundle> productBundles;

    /**
     * Gets the Application ID, as defined in Google Play (com.mycompany.app).
     *
     * @return the application ID
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the Application ID.
     *
     * @param applicationId the application ID
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Returns the JSON key for authenticating with the Google Play Developer API.
     *
     * @return the JSON key
     */
    public Map<String, Object> getJsonKey() {
        return jsonKey;
    }

    /**
     * Sets the JSON key for authenticating with the Google Play Developer API.
     *
     * @param jsonKey the JSON key
     */
    public void setJsonKey(Map<String, Object> jsonKey) {
        this.jsonKey = jsonKey;
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
     * Returns the product bundle for the given product ID.
     *
     * @param productId the product ID to look up
     * @return the matching product bundle, or null if not found
     * @deprecated use IAP SKU directly
     */
    @Deprecated
    public ProductBundle getProductBundle(final String productId) {
        if (getProductBundles() == null) {
            return null;
        }

        for (final ProductBundle productBundle : getProductBundles()) {
            if (Objects.equals(productBundle.getProductId(), productId)) {
                return productBundle;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        GooglePlayApplicationConfiguration that = (GooglePlayApplicationConfiguration) object;
        return Objects.equals(getApplicationId(), that.getApplicationId()) && Objects.equals(getJsonKey(), that.getJsonKey()) && Objects.equals(getProductBundles(), that.getProductBundles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getApplicationId(), getJsonKey(), getProductBundles());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GooglePlayApplicationConfiguration{");
        sb.append("applicationId='").append(applicationId).append('\'');
        sb.append(", jsonKey=").append(jsonKey);
        sb.append(", productBundles=").append(productBundles);
        sb.append('}');
        return sb.toString();
    }
}
