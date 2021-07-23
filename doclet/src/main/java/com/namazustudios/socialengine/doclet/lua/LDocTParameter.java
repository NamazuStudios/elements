package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;

public class LDocTParameter {

    private String type;

    private String comment;

    private final String name;

    private final ExposedModuleDefinition exposedModuleDefinition;

    public LDocTParameter(final ExposedModuleDefinition exposedModuleDefinition, final String name) {
        this.exposedModuleDefinition = exposedModuleDefinition;
        this.name = LOWER_CAMEL.to(exposedModuleDefinition.style().parameterCaseFormat(), name);
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocTParameter{");
        sb.append("type='").append(type).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", exposedModuleDefinition=").append(exposedModuleDefinition);
        sb.append('}');
        return sb.toString();
    }

}
