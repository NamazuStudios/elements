package com.namazustudios.socialengine.model.goods;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.ValidationGroups.Create;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Represents an Item that is a Digital Good.
 */
@ApiModel
public class Item implements Serializable {

    @Null(groups = Create.class)
    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WORD_ONLY)
    private String name;

    private Set<String> tags;

    @NotNull
    private String displayName;

    @NotNull
    private String description;

    private Map<String, String> metadata;

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
     * Get the tags for the Item.  The returned Set should be treated as immutable.
     *
     * @return The tags for an Item, or an empty Set if the Item does not have any tags.
     */
    public Set<String> getTags() {
        return tags == null ? new HashSet<>() : new HashSet<>(tags);
    }

    /**
     * Sets the tags for the Item.
     *
     * @param tags
     *     The tags for the Item.
     */
    public void setTags(Set<String> tags) {
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
    public Map<String, String> getMetadata() {
        return metadata == null ? new HashMap<>() : new HashMap<>(metadata);
    }

    /**
     * Sets the metadata of string key-value pairs for this Item.
     *
     * @param metadata
     *     The metadata for the Item
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata != null ? metadata : Collections.emptyMap();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Item item = (Item) o;

        if (getId() != null ? !getId().equals(item.getId()) : item.getId() != null) {
            return false;
        }
        if (!getName().equals(item.getName())) {
            return false;
        }
        if (getTags() != null ? !getTags().equals(item.getTags()) : item.getTags() != null) {
            return false;
        }
        if (!getDisplayName().equals(item.getDisplayName())) {
            return false;
        }
        if (!getDescription().equals(item.getDescription())) {
            return false;
        }
        return getMetadata() != null ? getMetadata().equals(item.getMetadata()) : item.getMetadata() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + getName().hashCode();
        result = 31 * result + (getTags() != null ? getTags().hashCode() : 0);
        result = 31 * result + getDisplayName().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + (getMetadata() != null ? getMetadata().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Item.class.getSimpleName() + "[", "]")
            .add("id='" + id + "'")
            .add("name='" + name + "'")
            .add("tags=" + tags)
            .add("displayName='" + displayName + "'")
            .add("description='" + description + "'")
            .add("metadata=" + metadata)
            .toString();
    }
}
