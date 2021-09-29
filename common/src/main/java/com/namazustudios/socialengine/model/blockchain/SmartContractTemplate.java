package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.application.Application;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

public class SmartContractTemplate {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the template itself.")
    public String id;

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The name given to this template.")
    public String name;

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The compiled smart contract.")
    public Object contractBinary;

    @NotNull(groups = ValidationGroups.Insert.class)
    @ApiModelProperty("The Application associated with this smart contract template.")
    private Application application;

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

    public Object getContractBinary() {
        return contractBinary;
    }

    public void setContractBinary(Object contractBinary) {
        this.contractBinary = contractBinary;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
