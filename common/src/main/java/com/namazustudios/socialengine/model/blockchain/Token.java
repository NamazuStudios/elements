package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

public class Token {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the token itself.")
    public String id;

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The name given to this token.")
    public String name;

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
}
