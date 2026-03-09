package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.List;

/**
 * Represents a request to invoke a method on a smart contract.
 *
 * @deprecated use the new smart contract API instead
 */
@Deprecated
public class InvokeContractRequest {

    /** Creates a new instance. */
    public InvokeContractRequest() {}

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

    /**
     * Returns the contract ID.
     *
     * @return the contract ID
     */
    public String getContractId() {
        return contractId;
    }

    /**
     * Sets the contract ID.
     *
     * @param contractId the contract ID
     */
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    /**
     * Returns the wallet ID.
     *
     * @return the wallet ID
     */
    public String getWalletId() {
        return walletId;
    }

    /**
     * Sets the wallet ID.
     *
     * @param walletId the wallet ID
     */
    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    /**
     * Returns the wallet password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the wallet password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the method name.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name.
     *
     * @param methodName the method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
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
}
