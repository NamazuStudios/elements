package com.namazustudios.socialengine.service.blockchain.evm;

import com.namazustudios.socialengine.dao.SmartContractDao;
import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.dao.WalletDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContractAddress;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.service.EvmSmartContractService;
import com.namazustudios.socialengine.service.blockchain.crypto.VaultCryptoUtilities;

import javax.inject.Inject;

import static java.lang.String.format;

public class SuperUserEvmSmartContractService implements EvmSmartContractService {

    private VaultDao vaultDao;

    private WalletDao walletDao;

    private SmartContractDao smartContractDao;

    private InvokerFactory invokerFactory;

    private VaultCryptoUtilities vaultCryptoUtilities;

    @Override
    public Resolution resolve(final String contractNameOrId, final BlockchainNetwork network) {

        final var smartContract = getSmartContractDao()
                .findSmartContractByNameOrId(contractNameOrId)
                .orElseThrow(() -> new IllegalArgumentException("No such contract: " + contractNameOrId));

        final var smartContractAddress = smartContract
                .getAddresses()
                .get(network);

        if (smartContractAddress == null) {
            final var msg = format("Contract %s does not contain address for %s", contractNameOrId, network);
            throw new InternalException(msg);
        }

        return new StandardResolution(smartContract, smartContractAddress);

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

    public InvokerFactory getInvokerFactory() {
        return invokerFactory;
    }

    @Inject
    public void setInvokerFactory(InvokerFactory invokerFactory) {
        this.invokerFactory = invokerFactory;
    }

    public VaultCryptoUtilities getVaultCryptoUtilities() {
        return vaultCryptoUtilities;
    }

    @Inject
    public void setVaultCryptoUtilities(VaultCryptoUtilities vaultCryptoUtilities) {
        this.vaultCryptoUtilities = vaultCryptoUtilities;
    }

    private class StandardResolution implements Resolution {

        private final Vault vault;

        private final SmartContract smartContract;

        private final SmartContractAddress smartContractAddress;

        public StandardResolution(final SmartContract smartContract,
                                  final SmartContractAddress smartContractAddress) {
            this.vault = smartContract.getVault();
            this.smartContract = smartContract;
            this.smartContractAddress = smartContractAddress;
        }

        public StandardResolution(final Vault vault,
                                  final SmartContract smartContract,
                                  final SmartContractAddress smartContractAddress) {
            this.vault = vault;
            this.smartContract = smartContract;
            this.smartContractAddress = smartContractAddress;
        }

        @Override
        public Invoker open() {

            final var key = vault.getKey();

            if (key.isEncrypted()) {
                throw new IllegalStateException("Vault is encrypted.");
            }

            return getInvokerFactory().newInvoker(vault, smartContract, smartContractAddress);

        }

        @Override
        public Invoker unlock(final String passphrase) {

            final var key = vault.getKey();

            final var unlocked = getVaultCryptoUtilities()
                    .decryptKey(key, passphrase)
                    .orElseThrow(() -> new IllegalStateException("Unable to unlock vault."));

            vault.setKey(unlocked);
            return getInvokerFactory().newInvoker(vault, smartContract, smartContractAddress);

        }

        @Override
        public Resolution vault(final String vaultId) {
            final var vault = getVaultDao().getVault(vaultId);
            return new StandardResolution(vault, smartContract, smartContractAddress);
        }

    }

}
