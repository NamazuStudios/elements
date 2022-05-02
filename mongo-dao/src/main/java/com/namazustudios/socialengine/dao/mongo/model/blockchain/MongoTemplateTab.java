package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@Embedded
public class MongoTemplateTab {

    @Property
    private String name;

    @Property
    private Integer tabOrder;
    @Property
    private List<MongoTemplateTabField> fields;

    public MongoTemplateTab() {
    }

    public MongoTemplateTab(String name, List<MongoTemplateTabField> fields) {
        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public Integer getTabOrder() {
        return tabOrder;
    }

    public void setTabOrder(Integer tabOrder) {
        this.tabOrder = tabOrder;
    }

    public List<MongoTemplateTabField>  getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoTemplateTab)) return false;
        MongoTemplateTab account = (MongoTemplateTab) o;
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
