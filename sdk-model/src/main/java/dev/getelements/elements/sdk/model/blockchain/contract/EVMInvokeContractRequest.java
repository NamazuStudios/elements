package dev.getelements.elements.sdk.model.blockchain.contract;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public class EVMInvokeContractRequest {

    @NotNull(groups = {ValidationGroups.Create.class})
    @Schema(description = "The unique elements ID of the contract to invoke a method on.")
    private String contractId;

    @NotNull(groups = {ValidationGroups.Create.class})
    @Schema(description = 
            "The elements wallet Id with funds to invoke the method. This will always use the default " +
            "account of the wallet.")
    private String walletId;

    @NotNull(groups = {ValidationGroups.Create.class})
    @Schema(description = "The password of the wallet with funds to mint.")
    private String password;

    @NotNull(groups = {ValidationGroups.Create.class})
    @Schema(description = "The name of the method to invoke.")
    private String methodName;

    @Schema(description = "The input types for the method. Must correspond to valid ABI types.")
    private List<String> inputTypes;

    @Schema(description = "The parameters for the method.")
    private List<Object> parameters;

    @Schema(description = "The output types for the method. Must correspond to valid ABI types.")
    private List<String> outputTypes;

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

    public List<String> getInputTypes() {
        return inputTypes;
    }

    public void setInputTypes(List<String> inputTypes) {
        this.inputTypes = inputTypes;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    public List<String> getOutputTypes() {
        return outputTypes;
    }

    public void setOutputTypes(List<String> outputTypes) {
        this.outputTypes = outputTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EVMInvokeContractRequest that = (EVMInvokeContractRequest) o;
        return Objects.equals(getContractId(), that.getContractId()) && Objects.equals(getWalletId(), that.getWalletId()) && Objects.equals(getPassword(), that.getPassword()) && Objects.equals(getMethodName(), that.getMethodName()) && Objects.equals(getInputTypes(), that.getInputTypes()) && Objects.equals(getParameters(), that.getParameters()) && Objects.equals(getOutputTypes(), that.getOutputTypes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContractId(), getWalletId(), getPassword(), getMethodName(), getInputTypes(), getParameters(), getOutputTypes());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EVMInvokeContractRequest{");
        sb.append("contractId='").append(contractId).append('\'');
        sb.append(", walletId='").append(walletId).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", methodName='").append(methodName).append('\'');
        sb.append(", inputTypes=").append(inputTypes);
        sb.append(", parameters=").append(parameters);
        sb.append(", outputTypes=").append(outputTypes);
        sb.append('}');
        return sb.toString();
    }

}
