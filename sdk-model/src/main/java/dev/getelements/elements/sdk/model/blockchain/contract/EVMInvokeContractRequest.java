package dev.getelements.elements.sdk.model.blockchain.contract;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/** Represents a request to invoke a method on an EVM smart contract. */
public class EVMInvokeContractRequest {

    /** Creates a new instance. */
    public EVMInvokeContractRequest() {}

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

    /**
     * Returns the unique elements ID of the contract.
     *
     * @return the contract id
     */
    public String getContractId() {
        return contractId;
    }

    /**
     * Sets the unique elements ID of the contract.
     *
     * @param contractId the contract id
     */
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    /**
     * Returns the elements wallet id.
     *
     * @return the wallet id
     */
    public String getWalletId() {
        return walletId;
    }

    /**
     * Sets the elements wallet id.
     *
     * @param walletId the wallet id
     */
    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    /**
     * Returns the password of the wallet.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the wallet.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the name of the method to invoke.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the name of the method to invoke.
     *
     * @param methodName the method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns the input types for the method.
     *
     * @return the input types
     */
    public List<String> getInputTypes() {
        return inputTypes;
    }

    /**
     * Sets the input types for the method.
     *
     * @param inputTypes the input types
     */
    public void setInputTypes(List<String> inputTypes) {
        this.inputTypes = inputTypes;
    }

    /**
     * Returns the parameters for the method.
     *
     * @return the parameters
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters for the method.
     *
     * @param parameters the parameters
     */
    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the output types for the method.
     *
     * @return the output types
     */
    public List<String> getOutputTypes() {
        return outputTypes;
    }

    /**
     * Sets the output types for the method.
     *
     * @param outputTypes the output types
     */
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
