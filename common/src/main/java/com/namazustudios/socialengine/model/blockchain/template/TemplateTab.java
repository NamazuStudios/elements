package com.namazustudios.socialengine.model.blockchain.template;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

public class TemplateTab {

    @ApiModelProperty("name")
    private String name;

    @ApiModelProperty("fields")
    private List<TemplateTabField> fields;

    public TemplateTab() {
    }

    public TemplateTab(String name, List<TemplateTabField> fields) {
        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public List<TemplateTabField>  getFields() {
        return fields;
    }

    public void  setFields(List<TemplateTabField> fields) {
         this.fields = fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateTab)) return false;
        TemplateTab account = (TemplateTab) o;
        return Objects.equals(getName(), getFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getFields());
    }

    @Override
    public String toString() {
        return "TemplateTab{" +
                "name='" + name + '\'' +
                ", fields='" + fields + '\'' +
                '}';
    }
}
