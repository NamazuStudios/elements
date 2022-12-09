package com.namazustudios.socialengine.service.blockchain.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.exception.NotImplementedException;
import com.namazustudios.socialengine.model.blockchain.BlockchainProtocol;
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
    public Wallet create(final Wallet wallet, int count) {

        if (count < 1) {
            throw new IllegalArgumentException("Must generate at least one wallet.");
        }

        final var generated = getObjectMapper().convertValue(wallet, Wallet.class);
        final var generator = getGenerator(wallet.getProtocol());
        final var identities = IntStream.range(0, count)
                .mapToObj(i -> generator.generate())
                .collect(toList());

        generated.setDefaultIdentity(0);
        generated.setIdentities(identities);

        return generated;

    }

    @Override
    public IdentityGenerator getGenerator(final BlockchainProtocol protocol) {

        if (protocol == null) {
            throw new IllegalArgumentException("Wallet must specify protocol.");
        }

        switch (protocol) {
            case NEO:
                return getNeoIdentityGeneratorProvider().get();
            case ETHEREUM:
                return getEthIdentityGeneratorProvider().get();
            case SOLANA:
                return getSolanaIdentityGeneratorProvider().get();
            default:
                throw new NotImplementedException("Unsupported Protocol: " + protocol);
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
