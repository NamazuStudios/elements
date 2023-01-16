package com.namazustudios.socialengine.service.blockchain.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class StandardWalletIdentityFactory implements WalletIdentityFactory {

    private ObjectMapper objectMapper;

    private Provider<EthIdentityGenerator> ethIdentityGeneratorProvider;

    private Provider<NeoIdentityGenerator> neoIdentityGeneratorProvider;

    private Provider<SolanaIdentityGenerator> solanaIdentityGeneratorProvider;

    @Override
    public IdentityGenerator getGenerator(final BlockchainApi api) {

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

    public Provider<EthIdentityGenerator> getEthIdentityGeneratorProvider() {
        return ethIdentityGeneratorProvider;
    }

    @Inject
    public void setEthIdentityGeneratorProvider(Provider<EthIdentityGenerator> ethIdentityGeneratorProvider) {
        this.ethIdentityGeneratorProvider = ethIdentityGeneratorProvider;
    }

    public Provider<NeoIdentityGenerator> getNeoIdentityGeneratorProvider() {
        return neoIdentityGeneratorProvider;
    }

    @Inject
    public void setNeoIdentityGeneratorProvider(Provider<NeoIdentityGenerator> neoIdentityGeneratorProvider) {
        this.neoIdentityGeneratorProvider = neoIdentityGeneratorProvider;
    }

    public Provider<SolanaIdentityGenerator> getSolanaIdentityGeneratorProvider() {
        return solanaIdentityGeneratorProvider;
    }

    @Inject
    public void setSolanaIdentityGeneratorProvider(Provider<SolanaIdentityGenerator> solanaIdentityGeneratorProvider) {
        this.solanaIdentityGeneratorProvider = solanaIdentityGeneratorProvider;
    }

}
