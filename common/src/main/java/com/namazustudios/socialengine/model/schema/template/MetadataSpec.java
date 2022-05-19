package com.namazustudios.socialengine.model.schema.template;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

@ApiModel
public class MetadataSpec {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the schema itself.")
    private String id;

    @ApiModelProperty("The Name of the schema.")
    private String name;

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The tabs of the metadata spec.")
    private List<TemplateTab> tabs ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TemplateTab> getTabs() {
        return this.tabs;
    }

    public void setTabs(List<TemplateTab> tabs) {

        this.tabs = tabs;
    }
}
