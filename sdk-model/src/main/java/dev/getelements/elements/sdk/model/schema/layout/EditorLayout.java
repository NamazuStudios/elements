package dev.getelements.elements.sdk.model.schema.layout;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/** Defines an editor layout element. */
@Schema(description = "Defines an editor layout element.")
public class EditorLayout {

    /** Creates a new instance. */
    public EditorLayout() {}

    private String key;

    private String title;

    private String placeholder;

    /**
     * Returns the key for this layout element.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key for this layout element.
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the title for this layout element.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title for this layout element.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the placeholder text for this layout element.
     *
     * @return the placeholder
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Sets the placeholder text for this layout element.
     *
     * @param placeholder the placeholder
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditorLayout that = (EditorLayout) o;
        return Objects.equals(getKey(), that.getKey()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getPlaceholder(), that.getPlaceholder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getTitle(), getPlaceholder());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EditorLayout{");
        sb.append("key='").append(key).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", placeholder='").append(placeholder).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
