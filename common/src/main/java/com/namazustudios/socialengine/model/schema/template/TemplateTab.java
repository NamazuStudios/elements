package com.namazustudios.socialengine.model.schema.template;

import io.swagger.annotations.ApiModelProperty;

import java.util.Map;
import java.util.Objects;

public class TemplateTab {

    @ApiModelProperty("name")
    private String name;

    @ApiModelProperty("tabOrder")
    private Integer tabOrder;

    @ApiModelProperty("fields")
    private Map<String, TemplateTabField> fields;

    public TemplateTab() {
    }

    public TemplateTab(String name, Map<String, TemplateTabField> fields) {
        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
         this.name = name;
    }

    public Integer getTabOrder() {
        return tabOrder;
    }

    public void setTabOrder(Integer tabOrder) {
        this.tabOrder = tabOrder;
    }

    public Map<String, TemplateTabField>  getFields() {
        return fields;
    }

    public void  setFields(Map<String, TemplateTabField> fields) {
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
