package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;

public class LDocStubMethod {

    private String summary;

    private String description;

    private final String name;

    private final Function<String, String> parameterNameTransform;

    private final List<LDocReturn> returnValues = new ArrayList<>();

    private final List<LDocParameter> parameters = new ArrayList<>();

    public LDocStubMethod(final String name) {
        this.name = name;
        this.parameterNameTransform = n -> n;
    }

    public LDocStubMethod(final String name, final ModuleDefinition moduleDefinition) {
        final var format = moduleDefinition.style().methodCaseFormat();
        this.name = LOWER_CAMEL.to(format, name);
        this.parameterNameTransform = n -> LOWER_CAMEL.to(moduleDefinition.style().parameterCaseFormat(), n);
    }

    public String getName() {
        return name;
    }

    public List<LDocReturn> getReturnValues() {
        return returnValues;
    }

    public LDocReturn addReturnValue() {
        final var ret = new LDocReturn();
        returnValues.add(ret);
        return ret;
    }

    public LDocReturn addReturnValue(final String type, final String description) {
        final var ret = addReturnValue();
        ret.setType(type);
        ret.setDescription(description);
        return ret;
    }

    public List<LDocParameter> getParameters() {
        return parameters;
    }

    public LDocParameter addParameter(final String name) {
        final var param = new LDocParameter(parameterNameTransform.apply(name));
        parameters.add(param);
        return param;
    }

    public LDocParameter addParameter(final String name, final String typeDescription, final String comment) {
        final var param = addParameter(name);
        param.setType(typeDescription);
        param.setDescription(comment);
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
        sb.append(", summary='").append(summary).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void writeCommentHeader(final DocRootWriter writer) {

        writer.printlnf("--- %s", getSummary())
              .printlnf("--")
              .printBlock("-- ", getDescription())
              .println("--");

        getParameters().forEach(p -> p.write(writer));
        writer.println("--");

        getReturnValues().forEach(r -> r.write(writer));

    }

}
