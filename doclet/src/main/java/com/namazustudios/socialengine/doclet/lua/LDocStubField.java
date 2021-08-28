package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRootWriter;

import static com.google.common.base.Strings.nullToEmpty;

public class LDocStubField {

    private final String name;

    private String type = "";

    private String summary = "";

    private String description = "";

    private String constantValue = "";

    public LDocStubField(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = nullToEmpty(type).trim();
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = nullToEmpty(summary).trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = nullToEmpty(description).trim();
    }

    public String getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(final Object constantValue) {
        this.constantValue = constantValue == null ? "" : constantValue.toString();
    }

    public void setConstantValue(final String constantValue) {
        this.constantValue = nullToEmpty(constantValue).trim();
    }

    public void writeCommentHeader(final DocRootWriter writer) {

        final var type = nullToEmpty(getType()).trim();
        final var summary = nullToEmpty(getSummary()).trim();
        final var description = nullToEmpty(getDescription()).trim();
        final var constantValue = nullToEmpty(getConstantValue()).trim();

        if (summary.isEmpty()) {
            writer.println("---");
        } else {
            writer.printlnf("--- %s", getSummary());
        }

        if (!description.isEmpty()) writer.printBlock("--", getDescription());

        final var sb = new StringBuilder("-- @field");

        if (!type.isEmpty()) sb.append("[type=").append(type).append(("]"));
        sb.append(" ").append(getName());

        if (!description.isEmpty()) sb.append(" ").append(description);
        if (!constantValue.isEmpty()) sb.append(" ").append("(Assigned Value: ").append(constantValue).append(")");

        writer.println(sb);

    }

    public void writeConstantStub(final DocRootWriter writer, final String table) {

        final var name = getName();
        final var constantValue = nullToEmpty(getConstantValue()).trim();

        try {
            final var val = Long.parseLong(constantValue);
            writer.printlnf("%s.%s=%d", table, name, val);
            return;
        } catch (NumberFormatException ex) {
            // Intentionally ignore exception
        }

        try {
            final var val = Double.parseDouble(constantValue);
            writer.printlnf("%s.%s=%f", table, name, val);
            return;
        } catch (NumberFormatException ex) {
            // Intentionally ignore exception
        }

        writer.printlnf("%s.%s=\"%s\"", table, name, constantValue);
        writer.println();

    }

}
