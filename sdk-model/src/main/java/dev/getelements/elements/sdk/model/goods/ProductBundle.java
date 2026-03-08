package dev.getelements.elements.sdk.model.goods;

import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ProductBundleReward;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Standalone goods-level product bundle. Maps a purchase-provider product (identified by schema + productId)
 * to a set of in-game rewards for a specific {@link Application}.
 */
@Schema(description = "Defines the digital goods issued when a specific product is purchased.")
public class ProductBundle {

    @Null(groups = Insert.class)
    @NotNull(groups = Update.class)
    @Schema(description = "The database id of this Product Bundle.")
    private String id;

    @NotNull
    @Schema(description = "The purchase provider schema in reverse-dns notation, e.g. com.apple.appstore.")
    private String schema;

    @Schema(description = "The application that owns this product bundle.")
    private Application application;

    @NotNull
    @Schema(description = "The product id as defined in the purchase provider's catalog.")
    private String productId;

    @Schema(description = "The title of the product bundle to display to end users.")
    private String displayName;

    @Schema(description = "The description of the product bundle to display to end users.")
    private String description;

    @Valid
    @Schema(description = "The list of rewards issued when this bundle is purchased.")
    private List<ProductBundleReward> productBundleRewards;

    @Schema(description = "Application-specific metadata.")
    private Map<String, Object> metadata;

    @Schema(description = "Whether or not the frontend should display this product bundle to end users.")
    private boolean display;

    @Schema(description = "Searchable tags associated with this product bundle.")
    private List<String> tags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

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

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductBundle that = (ProductBundle) o;
        return display == that.display &&
                Objects.equals(id, that.id) &&
                Objects.equals(schema, that.schema) &&
                Objects.equals(application, that.application) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(description, that.description) &&
                Objects.equals(productBundleRewards, that.productBundleRewards) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, schema, application, productId, displayName, description, productBundleRewards,
                metadata, display, tags);
    }

    @Override
    public String toString() {
        return "ProductBundle{" +
                "id='" + id + '\'' +
                ", schema='" + schema + '\'' +
                ", application=" + application +
                ", productId='" + productId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", productBundleRewards=" + productBundleRewards +
                ", metadata=" + metadata +
                ", display=" + display +
                ", tags=" + tags +
                '}';
    }

}
