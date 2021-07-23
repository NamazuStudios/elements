package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.ArrayList;
import java.util.List;

public class LDocStubHeader {

    private String description;

    private final List<String> authors = new ArrayList<>();

    private final ExposedModuleDefinition exposedModuleDefinition;

    public LDocStubHeader(final ExposedModuleDefinition exposedModuleDefinition) {
        this.exposedModuleDefinition = exposedModuleDefinition;
    }

    public String getTitle() {

        final var sb = new StringBuilder();

        final var deprecated = exposedModuleDefinition.deprecated();
        final var annotation = exposedModuleDefinition.annotation().value();

        sb.append("Module ").append(exposedModuleDefinition.value());

        if (!annotation.isAssignableFrom(ExposedBindingAnnotation.Undefined.class)) {
            sb.append(" ").append(annotation);
        }

        if (deprecated.deprecated()) {
            sb.append(" ").append(deprecated.value());
        }

        return sb.toString();

    }

    public String getModule() {
        return exposedModuleDefinition.value();
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocStubHeader{");
        sb.append("description='").append(description).append('\'');
        sb.append(", authors=").append(authors);
        sb.append(", exposedModuleDefinition=").append(exposedModuleDefinition);
        sb.append('}');
        return sb.toString();
    }

}
