package dev.getelements.elements.sdk.model.application;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Represents the set of items to issue to a user when they purchase a product id, defined at the
 * {@link ApplicationConfiguration} level. This allows multiple items with varying quantities to be issued upon
 * purchase.
 *
 * @deprecated Use {@link dev.getelements.elements.sdk.model.goods.ProductBundle} and the
 *     {@code ProductBundleDao} / {@code ProductBundleService} APIs instead.
 *     This embedded model is retained for backward compatibility and migration purposes only.
 */
@Deprecated
@Schema
public class ProductBundle implements Serializable {

    /** Creates a new instance. */
    public ProductBundle() {}

    @NotNull
    @Schema(description = "The platform-specific unique SKU/product identifier that, when purchased, will result in the " +
            "items in this product bundle to be issued to the user.")
    private String productId;

    @Schema(description = "The title of the product bundle to display to end users.")
    private String displayName;

    @Schema(description = "The description of the product bundle to display to end users.")
    private String description;

    @Valid
    @NotNull
    @Schema(description = "The list of product bundle rewards that will be issued to the user upon purchase.")
    private List<ProductBundleReward> productBundleRewards;

    @Schema(description = "Application-specific metadata.")
    private Map<String, Object> metadata;

    @NotNull
    @Schema(description = "Whether or not the frontend should display this product bundle to end users.")
    private boolean display;

    /**
     * Returns the platform-specific product identifier.
     *
     * @return the product ID
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the platform-specific product identifier.
     *
     * @param productId the product ID
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the display name of the product bundle.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the product bundle.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the description of the product bundle.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the product bundle.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the list of rewards in this product bundle.
     *
     * @return the product bundle rewards
     */
    public List<ProductBundleReward> getProductBundleRewards() {
        return productBundleRewards;
    }

    /**
     * Sets the list of rewards in this product bundle.
     *
     * @param productBundleRewards the product bundle rewards
     */
    public void setProductBundleRewards(List<ProductBundleReward> productBundleRewards) {
        this.productBundleRewards = productBundleRewards;
    }

    /**
     * Returns the application-specific metadata.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the application-specific metadata.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns whether the frontend should display this product bundle to end users.
     *
     * @return true if the bundle should be displayed
     */
    public boolean getDisplay() {
        return display;
    }

    /**
     * Sets whether the frontend should display this product bundle to end users.
     *
     * @param display true if the bundle should be displayed
     */
    public void setDisplay(boolean display) {
        this.display = display;
    }

    /**
     * Returns the reward for the given item ID, or null if not found.
     *
     * @param itemId the item ID to look up
     * @return the reward, or null
     */
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
