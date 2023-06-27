package dev.getelements.elements.doclet.lua;

import dev.getelements.elements.doclet.DocRootWriter;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.doclet.DocStrings.sanitize;
import static java.lang.String.format;

public class LDocReturn {

    private String type;

    private String description;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocTReturn{");
        sb.append("type='").append(type).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void write(final DocRootWriter writer) {

        final var prefix = "-- @return";
        final var sb = new StringBuilder(prefix);
        final var type = sanitize(getType());
        final var description = sanitize(getDescription());

        if (!type.isEmpty()) sb.append("[type=").append(type).append(("]"));
        if (!description.isEmpty()) sb.append(" ").append(description);
        if (sb.length() > prefix.length()) writer.println(sb);

    }

}
