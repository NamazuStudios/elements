package com.namazustudios.socialengine.model.blockchain.template;

import com.namazustudios.socialengine.BlockchainConstants;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class TemplateTabField {

    @ApiModelProperty("name")
    private String name;

    @ApiModelProperty("fieldType")
    private BlockchainConstants.TemplateFieldType fieldType = BlockchainConstants.TemplateFieldType.Enum;

    @ApiModelProperty("content")
    private String content;

    public TemplateTabField() {
    }

    public TemplateTabField(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public TemplateTabField(String name, BlockchainConstants.TemplateFieldType fieldType, String content) {
        this.name = name;
        this.fieldType = fieldType;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BlockchainConstants.TemplateFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(BlockchainConstants.TemplateFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateTabField)) return false;
        TemplateTabField contract = (TemplateTabField) o;
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
