package dev.getelements.elements.model.application;

import dev.getelements.elements.exception.DuplicateException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * Ties the {@link Application} model to one of its associated profiles as represented by the {@link ConfigurationCategory}
 * enumeration.  This is an abstract base class from which all application profiles are derived.
 *
 * Created by patricktwohig on 7/10/15.
 */
@ApiModel
public class ApplicationConfiguration implements Serializable {

    @ApiModelProperty("The database assigned ID for the application configuration.")
    private String id;

    @NotNull
    @ApiModelProperty("The category for the application configuration.")
    private ConfigurationCategory category;

    @ApiModelProperty("The application-configuration specific unique ID.  (Varies by ConfigurationCategory)")
    private String uniqueIdentifier;

    @ApiModelProperty("The parent application owning this configuration.")
    @NotNull
    private Application parent;

    @ApiModelProperty("The list of product bundles that may be rewarded upon successful IAP transactions.")
    private List<ProductBundle> productBundles;

    /**
     * Gets the actual profile ID.
     *
     * @return the profile ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the actual profile ID.
     *
     * @param id the profile ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the platfrom identifier.
     *
     * @return the identifier type
     */
    public ConfigurationCategory getCategory() {
        return category;
    }

    /**
     * Sets the category identifier.
     *
     * @param category the category identifier type.
     */
    public void setCategory(ConfigurationCategory category) {
        this.category = category;
    }

    /**
     * Gets the unique identifier for the category.
     *
     * @return
     */
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * Sets the unique identifier for the category.
     *
     * @param uniqueIdentifier
     */
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    /**
     * Gets the parent {@link Application}
     *
     * @return the parent
     */
    public Application getParent() {
        return parent;
    }

    /**
     * Sets the parent {@link Application}
     *
     * @param parent the parent
     */
    public void setParent(Application parent) {
        this.parent = parent;
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

    public void addProductBundle(final ProductBundle productBundle) {
        if (getProductBundles() == null) {
            setProductBundles(new ArrayList<>());
        }

        if (getProductBundle(productBundle.getProductId()) != null) {
            throw new DuplicateException("ProductBundle with productId " + productBundle.getProductId() + " already exists " +
                    "in ApplicationConfiguration " + getId());
        }

        getProductBundles().add(productBundle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationConfiguration that = (ApplicationConfiguration) o;
        return Objects.equals(getId(), that.getId()) &&
                getCategory() == that.getCategory() &&
                Objects.equals(getUniqueIdentifier(), that.getUniqueIdentifier()) &&
                Objects.equals(getParent(), that.getParent()) &&
                Objects.equals(getProductBundles(), that.getProductBundles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCategory(), getUniqueIdentifier(), getParent(), getProductBundles());
    }

    @Override
    public String toString() {
        return "ApplicationConfiguration{" +
                "id='" + id + '\'' +
                ", category=" + category +
                ", uniqueIdentifier='" + uniqueIdentifier + '\'' +
                ", parent=" + parent +
                ", productBundles=" + productBundles +
                '}';
    }
}
