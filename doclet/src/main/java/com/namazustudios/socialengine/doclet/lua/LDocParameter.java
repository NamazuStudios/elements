package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.CompoundDescription;
import com.namazustudios.socialengine.doclet.DocRootWriter;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.doclet.DocStrings.sanitize;
import static java.lang.String.format;

public class LDocParameter {

    private String type;

    private final String name;

    private final CompoundDescription description = new CompoundDescription(" ");

    public LDocParameter(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description.getDescription();
    }

    public void setDescription(final String description) {
        this.description.setDescription(description);
    }

    public void appendDescription(final String description) {
        this.description.appendDescription(description);
    }

    public void prependDescription(final String description) {
        this.description.prependDescription(description);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocTParameter{");
        sb.append("type='").append(type).append('\'');
        sb.append(", comment='").append(description).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void write(final DocRootWriter writer) {

        final var sb = new StringBuilder("-- @param");

        final var type = sanitize(getType());
        final var name = sanitize(getName());
        final var description = sanitize(getDescription());

        if (!type.isEmpty()) sb.append(format("[type=%s]", type));
        sb.append(" ").append(name);

        if (!description.isEmpty()) sb.append(" ").append(description);

        writer.println(sb);

    }

}
