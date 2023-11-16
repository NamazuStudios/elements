package dev.getelements.elements.dao.index;

import dev.getelements.elements.model.schema.template.TemplateTabField;

public class SimpleIndexStep implements IndexStep {

    private String indexName;

    private String description;

    private TemplateTabField templateTabField;

    @Override
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TemplateTabField getTemplateTabField() {
        return templateTabField;
    }

    public void setTemplateTabField(TemplateTabField templateTabField) {
        this.templateTabField = templateTabField;
    }

}
