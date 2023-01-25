package com.namazustudios.socialengine.service.blockchain.invoke.evm;

import com.namazustudios.socialengine.service.blockchain.invoke.InvocationScope;

import java.math.BigInteger;

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

}
