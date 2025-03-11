package dev.getelements.elements.sdk.model.schema.layout;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Defines an editor layout element.")
public class EditorLayout {

    private String key;

    private String title;

    private String placeholder;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlaceholder() {
        return placeholder;
    }

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
