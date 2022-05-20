package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class LDocStubFunction {

    private String summary = "";

    private String description = "";

    private final String name;

    private final ModuleDefinitionMetadata moduleDefinitionMetadata;

    private final List<LDocReturn> returnValues = new ArrayList<>();

    private final List<LDocParameter> parameters = new ArrayList<>();

    public LDocStubFunction(final String name, final ModuleDefinitionMetadata moduleDefinitionMetadata) {

        final var inputFormat = moduleDefinitionMetadata
            .getInputCodeStyle()
            .getMethodCaseFormat();

        final var outputFormat = moduleDefinitionMetadata
            .getOutputCodeStyle()
            .getMethodCaseFormat();

        this.name = inputFormat.to(outputFormat, name);
        this.moduleDefinitionMetadata = moduleDefinitionMetadata;

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

        final var inputFormat = moduleDefinitionMetadata
            .getOutputCodeStyle()
            .getParameterCaseFormat();

        final var outputFormat = moduleDefinitionMetadata
            .getOutputCodeStyle()
            .getParameterCaseFormat();

        final var param = new LDocParameter(inputFormat.to(outputFormat, name));

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

    public void writeMethodStub(final DocRootWriter writer, final String table) {
        writeExecutableStub(writer, table, ":");
    }


    public void writeFunctionStub(final DocRootWriter writer, final String table) {
        writeExecutableStub(writer, table, ".");
    }

    private void writeExecutableStub(final DocRootWriter writer, final String table, final String delimiter) {

        final var name = getName();

        final var params = getParameters()
            .stream()
            .map(LDocParameter::getName)
            .collect(joining(","));

        writer.printlnf("function %s%s%s(%s)", table, delimiter, name, params);

        try (var indent = writer.indent()) {
            writer.println("-- Stub ");
        }

        writer.println("end");
        writer.println();

    }

}
