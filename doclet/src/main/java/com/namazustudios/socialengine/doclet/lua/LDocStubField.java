package com.namazustudios.socialengine.doclet.lua;

import com.google.common.base.CaseFormat;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

public class LDocStubField {

    private final String name;

    private String type = "";

    private String comment = "";

    private String constantValue = "";

    public LDocStubField(final CaseFormat caseFormat,
                         final ModuleDefinition moduleDefinition,
                         final String name) {
        final var format = moduleDefinition.style().constantCaseFormat();
        this.name = caseFormat.to(format, name);
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }

}
