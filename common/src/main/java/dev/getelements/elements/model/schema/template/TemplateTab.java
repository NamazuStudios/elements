package dev.getelements.elements.model.schema.template;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class TemplateTab implements Serializable {

    @NotNull
    private String name;

    @NotNull
    @ApiModelProperty("The order of the tab.")
    private Integer tabOrder;

    @Valid
    @NotNull
    @ApiModelProperty("The fields of the tab.")
    private Map<String, TemplateTabField> fields;

    public TemplateTab() {}

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
