package com.namazustudios.socialengine.doclet.lua;

import com.google.common.base.CaseFormat;
import com.namazustudios.socialengine.doclet.CompoundDescription;
import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.base.Strings.*;
import static java.lang.String.format;

public class LDocStubModuleHeader {

    private String summary;

    private String description;

    private final List<String> authors = new ArrayList<>();

    private final CompoundDescription metadata = new CompoundDescription("\n");

    private final List<LDocStubField> fields = new ArrayList<>();

    private final ModuleDefinitionMetadata moduleDefinitionMetadata;

    public LDocStubModuleHeader(final ModuleDefinitionMetadata moduleDefinitionMetadata) {

        this.moduleDefinitionMetadata = moduleDefinitionMetadata;

        final var deprecation = moduleDefinitionMetadata.getDeprecationMetadata();

        if (deprecation.isDeprecated()) {
            final var message = format("Deprecated: %s", deprecation.getDeprecationMessage());
            getMetadata().appendDescription(message);
        }

    }

    public CompoundDescription getMetadata() {
        return metadata;
    }

    public void addMetadata(final String metadata) {
        this.metadata.appendDescription(metadata);
    }

    public String getModule() {
        return moduleDefinitionMetadata.getName();
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

    public List<String> getAuthors() {
        return authors;
    }

    public void addAuthor(final String author) {
        authors.add(author);
    }

    public List<LDocStubField> getFields() {
        return fields;
    }

    public LDocStubField addField(final String name, final CaseFormat source) {

        final var constantCaseFormat = moduleDefinitionMetadata
            .getOutputCodeStyle()
            .getConstantCaseFormat();

        final var field = new LDocStubField(source, constantCaseFormat, name);
        fields.add(field);

        return field;

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocStubHeader{");
        sb.append("description='").append(description).append('\'');
        sb.append(", authors=").append(authors);
        sb.append(", exposedModuleDefinition=").append(moduleDefinitionMetadata);
        sb.append('}');
        return sb.toString();
    }

    public void write(final DocRootWriter writer) {

        writer.printlnf("--- %s", nullToEmpty(getSummary()).trim())
              .println("--")
              .printBlock("-- ", nullToEmpty(getDescription()).trim())
              .println("--");

        getMetadata()
            .stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !isNullOrEmpty(s))
            .forEach(s -> writer.printlnf("-- %s", s));

        writer.printlnf("-- @module %s", moduleDefinitionMetadata.getName());

        getFields().forEach(f -> f.write(writer));

        getAuthors()
            .stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !isNullOrEmpty(s))
            .forEach(a -> writer.printlnf("-- @author %s", a));

        writer.println();

    }

}
