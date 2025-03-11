package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.service.blockchain.crypto.StandardWalletAccountFactory;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletAccountFactory;

public class TestWalletGenerator {

    public static void main(final String[] args) {

        final var factory = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(WalletAccountFactory.class).to(StandardWalletAccountFactory.class);
            }
        }).getInstance(WalletAccountFactory.class);

        final var account = factory.getGenerator(BlockchainApi.ETHEREUM).generate();

        System.out.println("API: " + BlockchainNetwork.ETHEREUM);
        System.out.println("Address: " + account.getAddress());
        System.out.println("Private Key: " + account.getPrivateKey());

    }

}
