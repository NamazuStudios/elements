package dev.getelements.elements.sdk.model.blockchain.contract;

import io.swagger.v3.oas.annotations.media.Schema;


import java.util.Objects;

/**
 * Represents the smart contract address.
 */
@Schema
public class SmartContractAddress {

    @Schema
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
