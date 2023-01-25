package com.namazustudios.socialengine.service.blockchain.invoke.evm;

import com.namazustudios.socialengine.dao.SmartContractDao;
import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.service.EvmSmartContractInvocationService;
import com.namazustudios.socialengine.service.SmartContractInvocationResolution;
import com.namazustudios.socialengine.service.blockchain.crypto.VaultCryptoUtilities;
import com.namazustudios.socialengine.service.blockchain.crypto.WalletCryptoUtilities;
import com.namazustudios.socialengine.service.blockchain.invoke.InvocationScope;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;

import javax.inject.Inject;
import javax.inject.Named;

import static java.lang.String.format;

public class SuperUserEvmSmartContractInvocationService implements EvmSmartContractInvocationService {

    private VaultDao vaultDao;

    private WalletDao walletDao;

    private SmartContractDao smartContractDao;

    private VaultCryptoUtilities vaultCryptoUtilities;

    private WalletCryptoUtilities walletCryptoUtilities;

    private IocResolver iocResolver;

    @Override
    public SmartContractInvocationResolution<Invoker> resolve(
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

        final var evmInvocationScope = new EvmInvocationScope();
        evmInvocationScope.setVault(vault);
        evmInvocationScope.setWallet(wallet);
        evmInvocationScope.setWalletAccount(walletAccount);
        evmInvocationScope.setSmartContract(smartContract);
        evmInvocationScope.setSmartContractAddress(smartContractAddress);
        evmInvocationScope.setBlockchainNetwork(blockchainNetwork);

        evmInvocationScope.setGasLimit(DEFAULT_GAS_LIMIT);
        evmInvocationScope.setGasPrice(DEFAULT_GAS_PRICE);

        return new StandardSmartContractInvocationResolution(evmInvocationScope);

    }

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

    public VaultCryptoUtilities getVaultCryptoUtilities() {
        return vaultCryptoUtilities;
    }

    @Inject
    public void setVaultCryptoUtilities(VaultCryptoUtilities vaultCryptoUtilities) {
        this.vaultCryptoUtilities = vaultCryptoUtilities;
    }

    public IocResolver getIocResolver() {
        return iocResolver;
    }

    @Inject
    public void setIocResolver(@Named(IOC_NAME) IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

    public WalletCryptoUtilities getWalletCryptoUtilities() {
        return walletCryptoUtilities;
    }

    @Inject
    public void setWalletCryptoUtilities(WalletCryptoUtilities walletCryptoUtilities) {
        this.walletCryptoUtilities = walletCryptoUtilities;
    }

    private class StandardSmartContractInvocationResolution implements SmartContractInvocationResolution {

        private EvmInvocationScope evmInvocationScope;

        public StandardSmartContractInvocationResolution(final EvmInvocationScope evmInvocationScope) {
            this.evmInvocationScope = evmInvocationScope;
        }

        @Override
        public Invoker open() {

            final var blockchainNetwork = evmInvocationScope.getBlockchainNetwork();

            final var invoker = getIocResolver().inject(
                    ScopedInvoker.class,
                    blockchainNetwork.iocName()
            );

            final var walletAccount = evmInvocationScope.getWalletAccount();

            if (walletAccount.isEncrypted()) {

                final var vaultKey = evmInvocationScope.getVault().getKey();

                if (vaultKey.isEncrypted()) {
                    throw new IllegalStateException("Vault key must not be encrypted.");
                }

                final var decryptedWalletAccount = getWalletCryptoUtilities()
                        .decrypt(vaultKey, evmInvocationScope.getWalletAccount())
                        .orElseThrow(() -> new IllegalArgumentException("Failed to decrypted wallet account."));

                evmInvocationScope.setWalletAccount(decryptedWalletAccount);

            }

            invoker.initialize(evmInvocationScope);

            return invoker;

        }

        @Override
        public Invoker unlock(final String passphrase) {

            final var key = evmInvocationScope.getVault().getKey();

            final var unlocked = getVaultCryptoUtilities()
                    .decryptKey(key, passphrase)
                    .orElseThrow(() -> new IllegalStateException("Unable to unlock vault."));

            evmInvocationScope.getVault().setKey(unlocked);
            return open();

        }

        @Override
        public SmartContractInvocationResolution vault(final String vaultId) {
            final var vault = getVaultDao().getVault(vaultId);
            evmInvocationScope.setVault(vault);
            return this;
        }

    }

}
