package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Represents the set of items to issue to a user when they purchase a product id, defined at the
 * {@link ApplicationConfiguration} level. This allows multiple items with varying quantities to be issued upon
 * purchase.
 *
 */
@Schema
public class ProductBundle implements Serializable {

    @NotNull
    @Schema(description = "The platform-specific unique SKU/product identifier that, when purchased, will result in the " +
            "items in this product bundle to be issued to the user.")
    private String productId;

    @Schema(description = "The title of the product bundle to display to end users.")
    private String displayName;

    @Schema(description = "The description of the product bundle to display to end users.")
    private String description;

    @NotNull
    @Schema(description = "The list of product bundle rewards that will be issued to the user upon purchase.")
    private List<ProductBundleReward> productBundleRewards;

    @Schema(description = "Application-specific metadata.")
    private Map<String, Object> metadata;

    @NotNull
    @Schema(description = "Whether or not the frontend should display this product bundle to end users.")
    private boolean display;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ProductBundleReward> getProductBundleRewards() {
        return productBundleRewards;
    }

    public void setProductBundleRewards(List<ProductBundleReward> productBundleRewards) {
        this.productBundleRewards = productBundleRewards;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean getDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public ProductBundleReward getProductBundleReward(final String itemId) {
        if (getProductBundleRewards() == null) {
            return null;
        }

        for (final ProductBundleReward productBundleReward : getProductBundleRewards()) {
            if (Objects.equals(productBundleReward.getItemId(), itemId)) {
                return productBundleReward;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductBundle that = (ProductBundle) o;
        return Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getDisplayName(), that.getDisplayName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getProductBundleRewards(), that.getProductBundleRewards()) &&
                Objects.equals(getMetadata(), that.getMetadata()) &&
                Objects.equals(getDisplay(), that.getDisplay());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductId(), getDisplayName(), getDescription(), getProductBundleRewards(),
                getMetadata(), getDisplay());
    }

    @Override
    public String toString() {
        return "ProductBundle{" +
                "productId='" + productId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", productBundleRewards=" + productBundleRewards +
                ", metadata=" + metadata +
                ", display=" + display +
                '}';
    }
}
