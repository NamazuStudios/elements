package dev.getelements.elements.model.goods;

import dev.getelements.elements.Constants;
import dev.getelements.elements.model.Taggable;
import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.ValidationGroups.Insert;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.*;

/**
 * Represents an Item that is a Digital Good.
 */
@ApiModel
public class Item implements Serializable, Taggable {

    @Null(groups = {Create.class, Insert.class})
    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    private String name;

    private List<String> tags;

    @NotNull
    private String displayName;

    @NotNull
    private String description;

    @NotNull
    private ItemCategory category;

    private Map<String, Object> metadata;

    private Boolean publicVisible;

    /**
     * Get the unique ID of the Item.
     *
     * @return the Item's unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * Set the unique ID of the Item.
     *
     * @param id
     *     the Item's unique ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the unique name of the Item
     *
     * @return The unique name for the Item
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Set the unique name for the Item
     *
     * @param name
     *     unique name for the Item
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Get the tags for the Item.  The returned List should be treated as immutable.
     *
     * @return The tags for an Item, or an empty List if the Item does not have any tags.
     */
    public List<String> getTags() {
        return tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    /**
     * Sets the tags for the Item.
     *
     * @param tags
     *     The tags for the Item.
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Gets the display name for this item, suitable for use at the presentation tier.
     *
     * @return The display name for the Item
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for this item.
     *
     * @param displayName
     *     The display name for this item
     */
    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }


    /**
     * Gets the long description for the Item
     *
     * @return The long description for the Item
     */
    @NotNull
    public String getDescription() {
        return description;
    }

    /**
     * Sets the long description for the Item
     *
     * @param description
     *     The long description for the Item
     */
    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    /**
     * Gets a copy of metadata of string key-value pairs for this Item.  Changes to the returned Map are not reflected
     * on this Item.
     *
     * @return The metadata for the Item
     */
    public Map<String, Object> getMetadata() {
        return metadata == null ? new HashMap<>() : new HashMap<>(metadata);
    }

    /**
     * Sets the metadata of string key-value pairs for this Item.
     *
     * @param metadata
     *     The metadata for the Item
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : Collections.emptyMap();
    }

    /**
     * Gets the {@link ItemCategory} of this item.
     *
     * @return the item category
     */
    public ItemCategory getCategory() {
        return category;
    }

    /**
     * Sets the {@link ItemCategory} of this item.
     *
     * @param category the item category
     */
    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    /**
     * Gets the visibility of this item.
     *
    * @return visibility
     */
    public Boolean getPublicVisible() {
        return publicVisible;
    }

    /**
     * Sets the visibility of this item.
     */
    public void setPublicVisible(Boolean publicVisible) {
        this.publicVisible = publicVisible;
    }

    public void addMetadata(final String name, final Object value) {

        if (getMetadata() == null) {
            setMetadata(new HashMap<>());
        }

        getMetadata().put(name, value);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return publicVisible == item.publicVisible && Objects.equals(id, item.id) && Objects.equals(name, item.name) && Objects.equals(tags, item.tags) && Objects.equals(displayName, item.displayName) && Objects.equals(description, item.description) && category == item.category && Objects.equals(metadata, item.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, tags, displayName, description, category, metadata, publicVisible);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", tags=" + tags +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", metadata=" + metadata +
                ", publicVisible=" + publicVisible +
                '}';
    }
}
