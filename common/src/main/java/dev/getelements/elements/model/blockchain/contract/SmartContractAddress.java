package dev.getelements.elements.model.blockchain.contract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Represents the smart contract address.
 */
@ApiModel
public class SmartContractAddress {

    @ApiModelProperty
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmartContractAddress that = (SmartContractAddress) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "SmartContractAddress{" +
                "address='" + address + '\'' +
                '}';
    }

}
