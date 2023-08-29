package dev.getelements.elements.service.blockchain.invoke.near;

import dev.getelements.elements.service.blockchain.invoke.InvocationScope;

import java.util.Objects;

public class NearInvocationScope extends InvocationScope {

    private long gasLimit;

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowInvocationScope{");
        sb.append("gasLimit=").append(gasLimit);
        sb.append('}');
        return sb.toString();
    }
}