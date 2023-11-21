package dev.getelements.elements.util;

import dev.getelements.elements.model.schema.template.MetadataSpec;
import dev.getelements.elements.model.schema.template.TemplateFieldType;
import dev.getelements.elements.model.schema.template.TemplateTab;
import dev.getelements.elements.model.schema.template.TemplateTabField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MetadataSpecBuilder {

    private final MetadataSpec spec;

    private final List<TemplateTab> tabs = new ArrayList<>();

    public static MetadataSpecBuilder with(final MetadataSpec spec) {
        return new MetadataSpecBuilder(spec);
    }

    public MetadataSpecBuilder() {
        this(new MetadataSpec());
    }

    public MetadataSpecBuilder(final MetadataSpec spec) {
        this.spec = spec;
    }

    public MetadataSpecBuilder name(String name) {
        spec.setName(name);
        return this;
    }

    public MetadataSpec buildSpec() {
        spec.setTabs(tabs);
        return spec;
    }

    public TabBuilder<MetadataSpecBuilder> tab() {
        return new TabBuilder<>(this, tabs);
    }

    public static class TabBuilder<ParentT> {

        private final ParentT parent;

        private final List<TemplateTab> tabs;

        private final TemplateTab templateTab = new TemplateTab();

        private final Map<String, TemplateTabField> fields = new LinkedHashMap<>();

        private TabBuilder(final ParentT parent, final List<TemplateTab> tabs) {
            this.tabs = tabs;
            this.parent = parent;
        }

        public TabBuilder<ParentT> name(final String name) {
            templateTab.setName(name);
            return this;
        }

        public TabBuilder<ParentT> tabOrder(final int tabOrder) {
            templateTab.setTabOrder(tabOrder);
            return this;
        }

        public FieldBuilder<ParentT> field() {
            return new FieldBuilder<>(this, fields);
        }

        public ParentT buildTab() {
            templateTab.setFields(fields);
            tabs.add(templateTab);
            return parent;
        }

    }

    public static class FieldBuilder<ParentT> {

        private final  TabBuilder<ParentT> parent;

        private final Map<String, TemplateTabField> fields;

        private final List<TemplateTab> tabs = new ArrayList<>();

        private final TemplateTabField field = new TemplateTabField();

        private FieldBuilder(final TabBuilder<ParentT> parent, final Map<String, TemplateTabField> fields) {
            this.fields = fields;
            this.parent = parent;
        }

        public FieldBuilder<ParentT> name(final String name) {
            field.setName(name);
            return this;
        }

        public FieldBuilder<ParentT> displayName(final String displayName) {
            field.setDisplayName(displayName);
            return this;
        }

        public FieldBuilder<ParentT> fieldType(final TemplateFieldType templateFieldType) {
            field.setFieldType(templateFieldType);
            return this;
        }

        public FieldBuilder<ParentT> isRequired(final Boolean isRequired) {
            field.setIsRequired(isRequired);
            return this;
        }

        public FieldBuilder<ParentT> placeholder(final String placeholder) {
            field.setPlaceHolder(placeholder);
            return this;
        }

        public FieldBuilder<ParentT> defaultValue(final String defaultValue) {
            field.setDefaultValue(defaultValue);
            return this;
        }

        public TabBuilder<FieldBuilder<ParentT>> tab() {
            return new TabBuilder<>(this, tabs);
        }

        public TabBuilder<ParentT> buildField() {

            final var name = field.getName();

            if (name == null) {
                throw new IllegalStateException("Field name not specified.");
            }

            field.setTabs(tabs);

            if (fields.containsKey(name)) {
                throw new IllegalStateException("Field with name already exists: " + name);
            }

            fields.put(name, field);
            return parent;

        }

    }

}
