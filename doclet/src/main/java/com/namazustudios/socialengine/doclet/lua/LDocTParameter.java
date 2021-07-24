package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;

public class LDocTParameter {

    private String type;

    private String comment;

    private final String name;

    private final ModuleDefinition moduleDefinition;

    public LDocTParameter(final ModuleDefinition moduleDefinition, final String name) {
        this.moduleDefinition = moduleDefinition;
        this.name = LOWER_CAMEL.to(moduleDefinition.style().parameterCaseFormat(), name);
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
        sb.append(", exposedModuleDefinition=").append(moduleDefinition);
        sb.append('}');
        return sb.toString();
    }

}
