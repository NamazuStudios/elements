package dev.getelements.elements.sdk.service.blockchain.invoke.near;

import dev.getelements.elements.sdk.service.blockchain.invoke.InvocationScope;

import java.math.BigInteger;
import java.util.Objects;

public class NearInvocationScope extends InvocationScope {

    private BigInteger gasLimit;

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NearInvocationScope that = (NearInvocationScope) o;
        return getGasLimit() == that.getGasLimit();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGasLimit());
    }

}