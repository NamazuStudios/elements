package com.namazustudios.socialengine.doclet.lua;

import com.google.common.base.CaseFormat;

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
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }

}
