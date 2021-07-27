package com.namazustudios.socialengine.doclet.lua;

import com.google.common.base.CaseFormat;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.ArrayList;
import java.util.List;

public class LDocStubModuleHeader {

        private String summary;

        private String description;

        private final List<String> authors = new ArrayList<>();

        private final List<LDocStubField> fields = new ArrayList<>();

        private final ModuleDefinition moduleDefinition;

        public LDocStubModuleHeader(final ModuleDefinition moduleDefinition) {
            this.moduleDefinition = moduleDefinition;
        }

        public String getMetadata() {

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

        public LDocStubField addField(final CaseFormat caseFormat, final String name) {
            final var field = new LDocStubField(caseFormat, moduleDefinition, name);
            fields.add(field);
            return field;
        }

        public LDocStubField addField(final CaseFormat caseFormat,
                                      final String typeDescription,
                                      final String name,
                                      final String comment,
                                      final Object constantValue) {

            final var field = addField(caseFormat, name);

            field.setComment(comment);
            field.setType(typeDescription);

            if (constantValue != null) {
                field.setComment(constantValue.toString());
            }

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

}
