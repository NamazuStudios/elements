package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.exception.DuplicateException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Represents the set of items to issue to a user when they purchase a product id, defined at the
 * {@link ApplicationConfiguration} level. This allows multiple items with varying quantities to be issued upon
 * purchase.
 *
 */
@ApiModel
public class ProductBundle implements Serializable {

    @NotNull
    @ApiModelProperty("The platform-specific unique SKU/product identifier that, when purchased, will result in the " +
            "items in this product bundle to be issued to the user.")
    private String productId;

    @ApiModelProperty("The title of the product bundle to display to end users.")
    private String displayName;

    @ApiModelProperty("The description of the product bundle to display to end users.")
    private String description;

    @NotNull
    @ApiModelProperty("The list of product bundle rewards that will be issued to the user upon purchase.")
    private List<ProductBundleReward> productBundleRewards;

    @ApiModelProperty("Application-specific metadata.")
    private Map<String, Object> metadata;

    @NotNull
    @ApiModelProperty("Whether or not the frontend should display this product bundle to end users.")
    private Boolean display;

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

    public Boolean getDisplay() {
        return display;
    }

    public void setDisplay(Boolean display) {
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

    public void addProductBundleReward(final ProductBundleReward productBundleReward) {
        if (getProductBundleRewards() == null) {
            setProductBundleRewards(new ArrayList<>());
        }

        if (getProductBundleReward(productBundleReward.getItemId()) != null) {
            throw new DuplicateException("ProductBundle with productId" + getProductId() + " already has a " +
                    "ProductBundleReward for itemId " + productBundleReward.getItemId());
        }

        getProductBundleRewards().add(productBundleReward);
    }

    public void addProductBundleReward(final String itemId, final Integer quantity) {
        final ProductBundleReward productBundleReward = new ProductBundleReward();
        productBundleReward.setItemId(itemId);
        productBundleReward.setQuantity(quantity);

        addProductBundleReward(productBundleReward);
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
