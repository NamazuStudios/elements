package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

public class InvokeContractRequest {

    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("The unique elements ID of the contract to invoke a method on.")
    private String contractId;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("The address of the account with funds to invoke.")
    private String address;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("The name of the method to invoke.")
    private String methodName;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("The parameters for the method.")
    private List<Object> parameters;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }
}
