package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.socialengine.BlockchainConstants;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@Embedded
public class MongoTemplateTabField {

    @Property
    private String name;

    @Property
    private BlockchainConstants.TemplateFieldType fieldType = BlockchainConstants.TemplateFieldType.Enum;

    @Property
    private String content;

    public MongoTemplateTabField() {
    }

    public MongoTemplateTabField(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public MongoTemplateTabField(String name, BlockchainConstants.TemplateFieldType fieldType, String content) {
        this.name = name;
        this.fieldType = fieldType;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public BlockchainConstants.TemplateFieldType getFieldType() {
        return fieldType;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoTemplateTabField)) return false;
        MongoTemplateTabField contract = (MongoTemplateTabField) o;
        return Objects.equals(getName(), contract.getName()) &&
                Objects.equals(getFieldType(), contract.getFieldType()) &&
                Objects.equals(getContent(), contract.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getFieldType(), getContent());
    }

    @Override
    public String toString() {
        return "TemplateTabField{" +
                "name='" + name + '\'' +
                ", fieldType=" + fieldType +
                ", content=" + content +
                '}';
    }
}
