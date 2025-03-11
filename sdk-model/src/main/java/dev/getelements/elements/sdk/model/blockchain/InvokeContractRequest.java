package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.List;

@Deprecated
public class InvokeContractRequest {

    @NotNull(groups = {ValidationGroups.Create.class})
    @Schema(description = "The unique elements ID of the contract to invoke a method on.")
    private String contractId;

    @NotNull(groups = {ValidationGroups.Create.class})
    @Schema(description = "The elements wallet Id with funds to invoke the method. This will always use the default account of the wallet.")
    private String walletId;

    @NotNull(groups = {ValidationGroups.Create.class})
    @Schema(description = "The password of the wallet with funds to mint.")
    private String password;

    @NotNull(groups = {ValidationGroups.Create.class})
    @Schema(description = "The name of the method to invoke.")
    private String methodName;

    @Schema(description = "The parameters for the method.")
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
