package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Represents the application profile and any associated metadata, such as APNS certificate
 * or other information.
 *
 * Created by patricktwohig on 5/23/17.
 */
@Schema(description = "Configuration for the iOS Application Configuration")
public class IosApplicationConfiguration extends ApplicationConfiguration implements Serializable {

    @NotNull
    private String applicationId;

    @Schema(description = "The list of product bundles that may be rewarded upon successful IAP transactions.")
    private List<ProductBundle> productBundles;

    /**
     * Gets the Application ID, as defined in the AppStore (com.mycompany.app)
     * @return the app id
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID, as deinfed in the AppStore (com.mycompany.app)
     * @param applicationId
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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
        IosApplicationConfiguration that = (IosApplicationConfiguration) object;
        return Objects.equals(getApplicationId(), that.getApplicationId()) && Objects.equals(getProductBundles(), that.getProductBundles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getApplicationId(), getProductBundles());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IosApplicationConfiguration{");
        sb.append("applicationId='").append(applicationId).append('\'');
        sb.append(", productBundles=").append(productBundles);
        sb.append('}');
        return sb.toString();
    }

}
