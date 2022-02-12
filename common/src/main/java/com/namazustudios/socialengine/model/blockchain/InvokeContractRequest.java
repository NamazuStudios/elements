package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

public class InvokeContractRequest {

    @NotNull(groups = {ValidationGroups.Create.class})
    @ApiModelProperty("The unique elements ID of the contract to invoke a method on.")
    private String contractId;

    @NotNull(groups = {ValidationGroups.Create.class})
    @ApiModelProperty("The elements wallet Id with funds to invoke the method. This will always use the default account of the wallet.")
    private String walletId;

    @NotNull(groups = {ValidationGroups.Create.class})
    @ApiModelProperty("The password of the wallet with funds to mint.")
    private String password;

    @NotNull(groups = {ValidationGroups.Create.class})
    @ApiModelProperty("The name of the method to invoke.")
    private String methodName;

    @ApiModelProperty("The parameters for the method.")
    private List<Object> parameters;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
