package com.namazustudios.socialengine.doclet.lua;

import com.google.common.base.CaseFormat;
import com.namazustudios.socialengine.doclet.DocRootWriter;

import static com.google.common.base.Strings.isNullOrEmpty;
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

    public LDocStubField(final CaseFormat source,
                         final CaseFormat destination,
                         final String name) {
        this (source.to(destination, name));
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

    public void setConstantValue(String constantValue) {
        this.constantValue = nullToEmpty(constantValue).trim();
    }

    public void write(final DocRootWriter writer) {
        if (!isNullOrEmpty(getName())) {
            final var sb = new StringBuilder("-- @field ").append(getName());
            if (!isNullOrEmpty(getType())) sb.append(" ").append(getType()).append(".");
            if (!isNullOrEmpty(getConstantValue())) sb.append(" \"").append(getConstantValue()).append("\".");
            if (!isNullOrEmpty(getSummary())) sb.append(" ").append(getSummary());
            if (!isNullOrEmpty(getDescription())) sb.append(" ").append(getSummary());
            writer.println(sb);
        }
    }

}
