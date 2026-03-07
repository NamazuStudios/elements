package dev.getelements.elements.service.iap;

import dev.getelements.elements.sdk.dao.IapSkuDao;
import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.RewardIssuanceDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.model.iap.IapSkuReward;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.iap.IapSkuService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.State.ISSUED;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.PERSISTENT;

public class UserIapSkuService implements IapSkuService {

    private static final Logger logger = LoggerFactory.getLogger(UserIapSkuService.class);

    private User user;

    private IapSkuDao iapSkuDao;

    private Provider<Transaction> transactionProvider;

    @Override
    public IapSku getIapSku(final String id) {
        throw new ForbiddenException("Unprivileged requests cannot access IAP SKU records.");
    }

    @Override
    public IapSku getIapSku(final String schema, final String productId) {
        throw new ForbiddenException("Unprivileged requests cannot access IAP SKU records.");
    }

    @Override
    public Pagination<IapSku> getIapSkus(final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access IAP SKU records.");
    }

    @Override
    public Pagination<IapSku> getIapSkus(final String schema, final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access IAP SKU records.");
    }

    @Override
    public IapSku createIapSku(final IapSku iapSku) {
        throw new ForbiddenException("Unprivileged requests cannot access IAP SKU records.");
    }

    @Override
    public IapSku updateIapSku(final IapSku iapSku) {
        throw new ForbiddenException("Unprivileged requests cannot access IAP SKU records.");
    }

    @Override
    public void deleteIapSku(final String id) {
        throw new ForbiddenException("Unprivileged requests cannot access IAP SKU records.");
    }

    @Override
    public void processVerifiedPurchase(
            final String schema,
            final String productId,
            final String originalTransactionId) {

        final IapSku sku;

        try {
            sku = getIapSkuDao().getIapSku(schema, productId);
        } catch (NotFoundException e) {
            logger.debug("No IAP SKU configured for schema={} productId={}", schema, productId);
            return;
        }

        getTransactionProvider().get().performAndCloseV(tx -> {

            final var riDao = tx.getDao(RewardIssuanceDao.class);
            final var itemDao = tx.getDao(ItemDao.class);
            final var rewards = sku.rewards();

            for (int i = 0; i < rewards.size(); i++) {

                final IapSkuReward reward = rewards.get(i);
                final var item = itemDao.getItemByIdOrName(reward.itemId());
                final int qty = DISTINCT.equals(item.getCategory()) ? 1 : reward.quantity();

                final var ri = new RewardIssuance();
                ri.setUser(getUser());
                ri.setItem(item);
                ri.setItemQuantity(qty);
                ri.setType(PERSISTENT);
                ri.setState(ISSUED);
                ri.setSource("IAP_SKU");
                ri.setContext("iap-sku:" + originalTransactionId + ":" + reward.itemId() + ":" + i);

                riDao.getOrCreateRewardIssuance(ri);

            }

        });

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public IapSkuDao getIapSkuDao() {
        return iapSkuDao;
    }

    @Inject
    public void setIapSkuDao(IapSkuDao iapSkuDao) {
        this.iapSkuDao = iapSkuDao;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
