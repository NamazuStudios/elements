package com.namazustudios.socialengine.doclet.lua;

import com.google.common.base.CaseFormat;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.ArrayList;
import java.util.List;

public class LDocStubMethod {

    private final String name;

    private final List<LDocTReturn> returnValues = new ArrayList<>();

    private final List<LDocTParameter> parameters = new ArrayList<>();

    private final ModuleDefinition moduleDefinition;

    private String summary;

    private String description;

    public LDocStubMethod(final String name) {
        this.name = name;
        this.moduleDefinition = null;
    }

    public LDocStubMethod(final ModuleDefinition moduleDefinition, final String name) {
        final var format = moduleDefinition.style().methodCaseFormat();
        this.moduleDefinition = moduleDefinition;
        this.name = CaseFormat.LOWER_CAMEL.to(format, name);
    }

    public List<LDocTReturn> getReturnValues() {
        return returnValues;
    }

    public LDocTReturn addReturnValue() {
        final var ret = new LDocTReturn();
        returnValues.add(ret);
        return ret;
    }

    public LDocTReturn addReturnValue(final String type, final String comment) {
        final var ret = addReturnValue();
        ret.setType(type);
        ret.setComment(comment);
        return ret;
    }

    public LDocTParameter addParameter(final String name) {
        final var param = new LDocTParameter(moduleDefinition, name);
        parameters.add(param);
        return param;
    }

    public LDocTParameter addParameter(final String name, final String typeDescription, final String comment) {
        final var param = addParameter(name);
        param.setType(typeDescription);
        param.setComment(comment);
        return param;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocStubMethod{");
        sb.append("name='").append(name).append('\'');
        sb.append(", returnValues=").append(returnValues);
        sb.append(", parameters=").append(parameters);
        sb.append(", moduleDefinition=").append(moduleDefinition);
        sb.append(", summary='").append(summary).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
