package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.CompoundDescription;
import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata;

import java.util.Objects;

import static com.google.common.base.Strings.*;
import static java.lang.String.format;

public class LDocStubModuleHeader {

    private String summary;

    private String description;

    private final CompoundDescription metadata = new CompoundDescription("\n");

    private final ModuleDefinitionMetadata moduleDefinitionMetadata;

    public LDocStubModuleHeader(final ModuleDefinitionMetadata moduleDefinitionMetadata) {

        this.moduleDefinitionMetadata = moduleDefinitionMetadata;

        final var deprecation = moduleDefinitionMetadata.getDeprecationMetadata();

        if (deprecation.isDeprecated()) {

            final var message = deprecation.getDeprecationMessage().trim();

            if (message.isEmpty()) {
                appendMetadata("<i>Deprecated.</i>");
            } else {
                appendMetadata(format("<i>Deprecated: %s</i>", message.replaceAll("[<>]*", "")));
            }

        }

    }

    public CompoundDescription getMetadata() {
        return metadata;
    }

    public void appendMetadata(final String metadata) {
        this.metadata.appendDescription(metadata);
    }

    public void prependMetadata(final String metadata) {
        this.metadata.prependDescription(metadata);
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

    public void addAuthor(final String author) {
        appendMetadata(format("@author %s", author));
    }

    public void write(final DocRootWriter writer) {

        final var summary = nullToEmpty(getSummary()).trim();
        final var description = nullToEmpty(getDescription()).trim();

        writer.printlnf("--- %s", summary);
        if (!description.isEmpty()) writer.printBlock("--", description);

        getMetadata()
            .stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !isNullOrEmpty(s))
            .forEach(s -> writer.printlnf("-- %s", s));

    }

}
