package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.dao.ChainWalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainProtocol;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class AbstractMongoChainWalletDao<WalletT> implements ChainWalletDao<WalletT> {

    private final Class<WalletT> walletTClass;

    private final BlockchainProtocol blockchainProtocol;

    private ObjectMapper objectMapper;

    private ValidationHelper validationHelper;

    private MongoWalletDao mongoWalletDao;

    public AbstractMongoChainWalletDao(final Class<WalletT> walletTClass,
                                       final BlockchainProtocol blockchainProtocol) {
        this.walletTClass = walletTClass;
        this.blockchainProtocol = blockchainProtocol;
    }

    @Override
    public Pagination<WalletT> getWallets(
            final int offset, final int count,
            final String userId, final List<BlockchainNetwork> networks) {
        return getWalletDao()
                .getWallets(offset, count, userId, blockchainProtocol, networks)
                .transform(w -> getObjectMapper().convertValue(w, walletTClass));
    }

    @Override
    public Optional<WalletT> findWallet(final String walletId, final String userId) {
        return getWalletDao()
                .findWallet(walletId, userId)
                .map(w -> blockchainProtocol.equals(w.getProtocol())
                        ? getObjectMapper().convertValue(w, walletTClass)
                        : null
                );
    }

    @Override
    public WalletT updateWallet(WalletT walletT) {
        getValidationHelper().validateModel(walletT, ValidationGroups.Update.class);
        final var wallet = getObjectMapper().convertValue(walletT, Wallet.class);
        final var result = getWalletDao().updateWallet(wallet);
        return getObjectMapper().convertValue(result, walletTClass);
    }

    @Override
    public WalletT createWallet(WalletT walletT) {
        getValidationHelper().validateModel(walletT, ValidationGroups.Insert.class);
        final var wallet = getObjectMapper().convertValue(walletT, Wallet.class);
        final var result = getWalletDao().createWallet(wallet);
        return getObjectMapper().convertValue(result, walletTClass);
    }

    @Override
    public void deleteWallet(final String walletId, final String userId) {
        getWalletDao().deleteWallet(walletId, userId, blockchainProtocol);
    }

    public MongoWalletDao getWalletDao() {
        return mongoWalletDao;
    }

    @Inject
    public void setWalletDao(MongoWalletDao mongoWalletDao) {
        this.mongoWalletDao = mongoWalletDao;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
