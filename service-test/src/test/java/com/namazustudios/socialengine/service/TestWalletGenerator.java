package com.namazustudios.socialengine.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.service.blockchain.crypto.StandardWalletIdentityFactory;
import com.namazustudios.socialengine.service.blockchain.crypto.WalletIdentityFactory;

public class TestWalletGenerator {

    public static void main(final String[] args) {

        final var factory = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(WalletIdentityFactory.class).to(StandardWalletIdentityFactory.class);
            }
        }).getInstance(WalletIdentityFactory.class);

        final var account = factory.getGenerator(BlockchainApi.ETHEREUM).generate();

        System.out.println("API: " + BlockchainNetwork.ETHEREUM);
        System.out.println("Address: " + account.getAddress());
        System.out.println("Private Key: " + account.getPrivateKey());

    }

}
