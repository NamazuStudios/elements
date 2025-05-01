package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.io.Serializable;
import java.util.*;

/**
 * Ties the {@link Application} model to one of its associated profiles as represented by the {@link ConfigurationCategory}
 * enumeration.  This is an abstract base class from which all application profiles are derived.
 *
 * Created by patricktwohig on 7/10/15.
 */
@Schema
public class ApplicationConfiguration implements Serializable {

    @Schema(description = "The database assigned ID for the application configuration.")
    @Null(groups = Insert.class)
    @NotNull(groups = Update.class)
    private String id;

    @NotNull
    @Schema(description = "The category for the application configuration.")
    private ConfigurationCategory category;

    @NotNull
    @Schema(description = "The application-configuration specific unique ID.  (Varies by ConfigurationCategory)")
    private String uniqueIdentifier;

    @NotNull
    @Schema(description = "The parent application owning this configuration.")
    private Application parent;

    @Schema(description = "The list of product bundles that may be rewarded upon successful IAP transactions.")
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
