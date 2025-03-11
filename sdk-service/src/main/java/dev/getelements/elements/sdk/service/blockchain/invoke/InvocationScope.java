package dev.getelements.elements.sdk.service.blockchain.invoke;

import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContract;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContractAddress;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;
import dev.getelements.elements.sdk.model.blockchain.wallet.WalletAccount;

import java.util.Objects;

public class InvocationScope {

    private Vault vault;

    private Wallet wallet;

    private WalletAccount walletAccount;

    private SmartContract smartContract;

    private BlockchainNetwork blockchainNetwork;

    private SmartContractAddress smartContractAddress;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvocationScope that = (InvocationScope) o;
        return Objects.equals(getVault(), that.getVault()) && Objects.equals(getWallet(), that.getWallet()) && Objects.equals(getWalletAccount(), that.getWalletAccount()) && Objects.equals(getSmartContract(), that.getSmartContract()) && getBlockchainNetwork() == that.getBlockchainNetwork() && Objects.equals(getSmartContractAddress(), that.getSmartContractAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVault(), getWallet(), getWalletAccount(), getSmartContract(), getBlockchainNetwork(), getSmartContractAddress());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InvocationScope{");
        sb.append("vault=").append(vault);
        sb.append(", wallet=").append(wallet);
        sb.append(", walletAccount=").append(walletAccount);
        sb.append(", smartContract=").append(smartContract);
        sb.append(", blockchainNetwork=").append(blockchainNetwork);
        sb.append(", smartContractAddress=").append(smartContractAddress);
        sb.append('}');
        return sb.toString();
    }

}
