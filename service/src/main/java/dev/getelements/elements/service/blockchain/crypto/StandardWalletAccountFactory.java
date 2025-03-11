package dev.getelements.elements.service.blockchain.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.model.exception.NotImplementedException;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletAccountFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class StandardWalletAccountFactory implements WalletAccountFactory {

    private ObjectMapper objectMapper;

    private Provider<EthAccountGenerator> ethIdentityGeneratorProvider;

    private Provider<NeoAccountGenerator> neoIdentityGeneratorProvider;

    private Provider<FlowAccountGenerator> flowAccountGeneratorProvider;

    private Provider<SolanaAccountGenerator> solanaIdentityGeneratorProvider;

    private Provider<NearAccountGenerator> nearIdentityGeneratorProvider;

    @Override
    public WalletAccountFactory.AccountGenerator getGenerator(final BlockchainApi api) {

        if (api == null) {
            throw new IllegalArgumentException("Wallet must specify protocol.");
        }

        switch (api) {
            case NEO:
                return getNeoIdentityGeneratorProvider().get();
            case ETHEREUM:
                return getEthIdentityGeneratorProvider().get();
            case SOLANA:
                return getSolanaIdentityGeneratorProvider().get();
            case FLOW:
                return getFlowAccountGeneratorProvider().get();
            case NEAR:
                return getNearIdentityGeneratorProvider().get();
            default:
                throw new NotImplementedException("Unsupported API: " + api);
        }

    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Provider<EthAccountGenerator> getEthIdentityGeneratorProvider() {
        return ethIdentityGeneratorProvider;
    }

    @Inject
    public void setEthIdentityGeneratorProvider(Provider<EthAccountGenerator> ethIdentityGeneratorProvider) {
        this.ethIdentityGeneratorProvider = ethIdentityGeneratorProvider;
    }

    public Provider<NeoAccountGenerator> getNeoIdentityGeneratorProvider() {
        return neoIdentityGeneratorProvider;
    }

    @Inject
    public void setNeoIdentityGeneratorProvider(Provider<NeoAccountGenerator> neoIdentityGeneratorProvider) {
        this.neoIdentityGeneratorProvider = neoIdentityGeneratorProvider;
    }

    public Provider<FlowAccountGenerator> getFlowAccountGeneratorProvider() {
        return flowAccountGeneratorProvider;
    }

    @Inject
    public void setFlowAccountGeneratorProvider(Provider<FlowAccountGenerator> flowAccountGeneratorProvider) {
        this.flowAccountGeneratorProvider = flowAccountGeneratorProvider;
    }

    public Provider<SolanaAccountGenerator> getSolanaIdentityGeneratorProvider() {
        return solanaIdentityGeneratorProvider;
    }

    @Inject
    public void setSolanaIdentityGeneratorProvider(Provider<SolanaAccountGenerator> solanaIdentityGeneratorProvider) {
        this.solanaIdentityGeneratorProvider = solanaIdentityGeneratorProvider;
    }

    public Provider<NearAccountGenerator> getNearIdentityGeneratorProvider() {
        return nearIdentityGeneratorProvider;
    }

    @Inject
    public void setNearIdentityGeneratorProvider(Provider<NearAccountGenerator> nearIdentityGeneratorProvider) {
        this.nearIdentityGeneratorProvider = nearIdentityGeneratorProvider;
    }

}
