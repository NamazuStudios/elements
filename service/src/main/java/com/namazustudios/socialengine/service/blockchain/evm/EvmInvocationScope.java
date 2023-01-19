package com.namazustudios.socialengine.service.blockchain.evm;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContractAddress;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;

import java.math.BigInteger;

public class EvmInvocationScope {

    private Vault vault;

    private Wallet wallet;

    private WalletAccount walletAccount;

    private SmartContract smartContract;

    private BlockchainNetwork blockchainNetwork;

    private SmartContractAddress smartContractAddress;

    private BigInteger gasLimit;

    private BigInteger gasPrice;

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public WalletAccount getWalletAccount() {
        return walletAccount;
    }

    public void setWalletAccount(WalletAccount walletAccount) {
        this.walletAccount = walletAccount;
    }

    public SmartContract getSmartContract() {
        return smartContract;
    }

    public void setSmartContract(SmartContract smartContract) {
        this.smartContract = smartContract;
    }

    public BlockchainNetwork getBlockchainNetwork() {
        return blockchainNetwork;
    }

    public void setBlockchainNetwork(BlockchainNetwork blockchainNetwork) {
        this.blockchainNetwork = blockchainNetwork;
    }

    public SmartContractAddress getSmartContractAddress() {
        return smartContractAddress;
    }

    public void setSmartContractAddress(SmartContractAddress smartContractAddress) {
        this.smartContractAddress = smartContractAddress;
    }

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
