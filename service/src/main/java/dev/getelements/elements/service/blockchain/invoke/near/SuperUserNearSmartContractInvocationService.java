package dev.getelements.elements.service.blockchain.invoke.near;

import dev.getelements.elements.dao.SmartContractDao;
import dev.getelements.elements.service.NearSmartContractInvocationService;
import dev.getelements.elements.service.SmartContractInvocationResolution;
import dev.getelements.elements.service.blockchain.crypto.VaultCryptoUtilities;
import dev.getelements.elements.service.blockchain.crypto.WalletCryptoUtilities;
import dev.getelements.elements.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.service.blockchain.invoke.StandardSmartContractInvocationResolution;
import dev.getelements.elements.service.blockchain.invoke.SuperUserSmartContractInvocationService;

import javax.inject.Inject;
import javax.inject.Provider;


public class SuperUserNearSmartContractInvocationService
        extends SuperUserSmartContractInvocationService<NearInvocationScope, NearSmartContractInvocationService.Invoker>
        implements NearSmartContractInvocationService {

    private SmartContractDao smartContractDao;

    private VaultCryptoUtilities vaultCryptoUtilities;

    private WalletCryptoUtilities walletCryptoUtilities;

    private ScopedInvoker.Factory<NearInvocationScope, Invoker> scopedInvokerFactory;

    private Provider<StandardSmartContractInvocationResolution<NearInvocationScope, Invoker>> resolutionProvider;

    @Override
    protected NearInvocationScope newInvocationScope() {
        final var scope = new NearInvocationScope();
        return scope;
    }

    @Override
    protected SmartContractInvocationResolution<Invoker> newResolution(final NearInvocationScope nearInvocationScope) {
        final var resolution = getResolutionProvider().get();
        resolution.setScope(nearInvocationScope);
        resolution.setScopedInvokerFactory(getScopedInvokerFactory());
        return resolution;
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

    public ScopedInvoker.Factory<NearInvocationScope, Invoker> getScopedInvokerFactory() {
        return scopedInvokerFactory;
    }

    @Inject
    public void setScopedInvokerFactory(ScopedInvoker.Factory<NearInvocationScope, Invoker> scopedInvokerFactory) {
        this.scopedInvokerFactory = scopedInvokerFactory;
    }

    public WalletCryptoUtilities getWalletCryptoUtilities() {
        return walletCryptoUtilities;
    }

    @Inject
    public void setWalletCryptoUtilities(WalletCryptoUtilities walletCryptoUtilities) {
        this.walletCryptoUtilities = walletCryptoUtilities;
    }

    public Provider<StandardSmartContractInvocationResolution<NearInvocationScope, Invoker>> getResolutionProvider() {
        return resolutionProvider;
    }

    @Inject
    public void setResolutionProvider(Provider<StandardSmartContractInvocationResolution<NearInvocationScope, Invoker>> resolutionProvider) {
        this.resolutionProvider = resolutionProvider;
    }
    
}