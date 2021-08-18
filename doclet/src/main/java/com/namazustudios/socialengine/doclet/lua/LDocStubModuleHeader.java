package com.namazustudios.socialengine.doclet.lua;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.google.common.base.Strings.*;

public class LDocStubModuleHeader {

    private String summary;

    private String description;

    private final List<String> authors = new ArrayList<>();

    private final List<String> metadata = new ArrayList<>();

    private final List<LDocStubField> fields = new ArrayList<>();

    private final ModuleDefinition moduleDefinition;

    public LDocStubModuleHeader(final ModuleDefinition moduleDefinition) {
        this.moduleDefinition = moduleDefinition;
        addMetadata(moduleDefinition.toString());
    }

    private String buildMetadataForDefinition() {

        final var sb = new StringBuilder();

        final var deprecated = moduleDefinition.deprecated();
        final var annotation = moduleDefinition.annotation().value();

        sb.append("Module ").append(moduleDefinition.value());

        if (!annotation.isAssignableFrom(ExposedBindingAnnotation.Undefined.class)) {
            sb.append(" ").append(annotation.getSimpleName());
        }

        if (deprecated.deprecated()) {
            sb.append(" ").append(deprecated.value());
        }

        return sb.toString();

    }

    public List<String> getMetadata() {
        return metadata;
    }

    public void addMetadata(final String metadata) {
        this.metadata.add(metadata);
    }

    public String getModule() {
        return moduleDefinition.value();
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
        final var field = new LDocStubField(source, moduleDefinition.style().constantCaseFormat(), name);
        fields.add(field);
        return field;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocStubHeader{");
        sb.append("description='").append(description).append('\'');
        sb.append(", authors=").append(authors);
        sb.append(", exposedModuleDefinition=").append(moduleDefinition);
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

        var module = Stream.of(moduleDefinition.value().split("\\."))
            .reduce((first, second) -> second)
            .stream()
            .findFirst()
            .get();

        writer.printlnf("-- @module %s", module);

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
