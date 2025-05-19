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

    private String applicationId;

    private Map<String, Object> jsonKey;

    @Schema(description = "The list of product bundles that may be rewarded upon successful IAP transactions.")
    private List<ProductBundle> productBundles;

    /**
     * Gets the Application ID, as defined in Google Play (com.mycompany.app)
     *
     * @return
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the Application ID
     *
     * @param applicationId
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Map<String, Object> getJsonKey() {
        return jsonKey;
    }

    public void setJsonKey(Map<String, Object> jsonKey) {
        this.jsonKey = jsonKey;
    }

    public List<ProductBundle> getProductBundles() {
        return productBundles;
    }

    public void setProductBundles(List<ProductBundle> productBundles) {
        this.productBundles = productBundles;
    }

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
