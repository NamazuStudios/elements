package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;

public class LDocStubClassHeader {

    private String summary;

    private String description;

    private final List<String> authors = new ArrayList<>();

    private final List<LDocStubField> fields = new ArrayList<>();

    private final ModuleDefinitionMetadata moduleDefinitionMetadata;

    public LDocStubClassHeader(final ModuleDefinitionMetadata moduleDefinitionMetadata) {
        this.moduleDefinitionMetadata = moduleDefinitionMetadata;
    }

    public String getName() {
        return moduleDefinitionMetadata.getName();
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void addAuthor(final String author) {
        authors.add(author);
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

    public void setDescription(final String description) {
        this.description = description;
    }

    public void addExtraDescription(final String extraDescription) {
        if (getDescription() == null) {
            setDescription(extraDescription);
        } else {
            setDescription(format("%s\n%s", getDescription(), extraDescription));
        }
    }

    public void addExtraDescriptionFormat(final String extraDescriptionFmt, final Object ... args) {
        final var extra = format(extraDescriptionFmt, args);
        addExtraDescription(extra);
    }

    public List<LDocStubField> getFields() {
        return fields;
    }

    public LDocStubField addField(final String name) {
        final var field = new LDocStubField(name);
        fields.add(field);
        return field;
    }

    public void write(final DocRootWriter writer) {
        writer.printlnf("--- %s", nullToEmpty(getSummary()).trim());
        writer.println();

    }

}
