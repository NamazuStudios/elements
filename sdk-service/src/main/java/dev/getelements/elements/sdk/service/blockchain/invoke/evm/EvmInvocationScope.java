package dev.getelements.elements.sdk.service.blockchain.invoke.evm;

import dev.getelements.elements.sdk.service.blockchain.invoke.InvocationScope;

import java.math.BigInteger;
import java.util.Objects;

public class EvmInvocationScope extends InvocationScope {

    private BigInteger gasLimit;

    private BigInteger gasPrice;

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EvmInvocationScope that = (EvmInvocationScope) o;
        return Objects.equals(gasLimit, that.gasLimit) && Objects.equals(gasPrice, that.gasPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gasLimit, gasPrice);
    }

}
