package com.namazustudios.socialengine.service.blockchain.invoke;

import com.namazustudios.socialengine.dao.SmartContractDao;
import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.service.SmartContractInvocationResolution;
import com.namazustudios.socialengine.service.SmartContractInvocationService;

import javax.inject.Inject;

import static java.lang.String.format;

public abstract
        class SuperUserSmartContractInvocationService<InvocationScopeT extends InvocationScope, InvokerT>
        implements SmartContractInvocationService<InvokerT> {

    private VaultDao vaultDao;

    private WalletDao walletDao;

    private SmartContractDao smartContractDao;

    @Override
    public SmartContractInvocationResolution<InvokerT> resolve(
            final String contractNameOrId,
            final BlockchainNetwork blockchainNetwork) {

        final var smartContract = getSmartContractDao()
                .findSmartContractByNameOrId(contractNameOrId)
                .orElseThrow(() -> new IllegalArgumentException("No such contract: " + contractNameOrId));

        final var smartContractAddress = smartContract
                .getAddresses()
                .get(blockchainNetwork);

        if (smartContractAddress == null) {
            final var msg = format("Contract %s does not contain address for %s", contractNameOrId, blockchainNetwork);
            throw new InternalException(msg);
        }

        final var vault = smartContract.getVault();

        final var wallet = getWalletDao().getSingleWalletFromVaultForNetwork(
                vault.getId(),
                blockchainNetwork
        );

        final var preferredAccount = wallet.getPreferredAccount();
        final var walletAccount = wallet.getAccounts().get(preferredAccount);

        final var scope = newInvocationScope();
        scope.setVault(vault);
        scope.setWallet(wallet);
        scope.setWalletAccount(walletAccount);
        scope.setSmartContract(smartContract);
        scope.setSmartContractAddress(smartContractAddress);
        scope.setBlockchainNetwork(blockchainNetwork);

        return newResolution(scope);

    }

    protected abstract InvocationScopeT newInvocationScope();

    protected abstract SmartContractInvocationResolution<InvokerT> newResolution(InvocationScopeT scopeT);

    public VaultDao getVaultDao() {
        return vaultDao;
    }

    @Inject
    public void setVaultDao(VaultDao vaultDao) {
        this.vaultDao = vaultDao;
    }

    public WalletDao getWalletDao() {
        return walletDao;
    }

    @Inject
    public void setWalletDao(WalletDao walletDao) {
        this.walletDao = walletDao;
    }

    public SmartContractDao getSmartContractDao() {
        return smartContractDao;
    }

    @Inject
    public void setSmartContractDao(SmartContractDao smartContractDao) {
        this.smartContractDao = smartContractDao;
    }

}
