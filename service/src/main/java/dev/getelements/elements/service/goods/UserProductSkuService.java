package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ProductSkuDao;
import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.RewardIssuanceDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductSku;
import dev.getelements.elements.sdk.model.goods.ProductSkuReward;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.goods.ProductSkuService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.State.ISSUED;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.PERSISTENT;

public class UserProductSkuService implements ProductSkuService {

    private static final Logger logger = LoggerFactory.getLogger(UserProductSkuService.class);

    private User user;

    private ProductSkuDao productSkuDao;

    private Provider<Transaction> transactionProvider;

    @Override
    public ProductSku getProductSku(final String id) {
        throw new ForbiddenException("Unprivileged requests cannot access Product SKU records.");
    }

    @Override
    public ProductSku getProductSku(final String schema, final String productId) {
        throw new ForbiddenException("Unprivileged requests cannot access Product SKU records.");
    }

    @Override
    public Pagination<ProductSku> getProductSkus(final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access Product SKU records.");
    }

    @Override
    public Pagination<ProductSku> getProductSkus(final String schema, final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access Product SKU records.");
    }

    @Override
    public ProductSku createProductSku(final ProductSku productSku) {
        throw new ForbiddenException("Unprivileged requests cannot access Product SKU records.");
    }

    @Override
    public ProductSku updateProductSku(final ProductSku productSku) {
        throw new ForbiddenException("Unprivileged requests cannot access Product SKU records.");
    }

    @Override
    public void deleteProductSku(final String id) {
        throw new ForbiddenException("Unprivileged requests cannot access Product SKU records.");
    }

    @Override
    public void processVerifiedPurchase(
            final String schema,
            final String productId,
            final String originalTransactionId) {

        final ProductSku sku;

        try {
            sku = getProductSkuDao().getProductSku(schema, productId);
        } catch (NotFoundException e) {
            logger.debug("No Product SKU configured for schema={} productId={}", schema, productId);
            return;
        }

        getTransactionProvider().get().performAndCloseV(tx -> {

            final var riDao = tx.getDao(RewardIssuanceDao.class);
            final var itemDao = tx.getDao(ItemDao.class);
            final var rewards = sku.rewards();

            for (int i = 0; i < rewards.size(); i++) {

                final ProductSkuReward reward = rewards.get(i);
                final var item = itemDao.getItemByIdOrName(reward.itemId());
                final int qty = DISTINCT.equals(item.getCategory()) ? 1 : reward.quantity();

                final var ri = new RewardIssuance();
                ri.setUser(getUser());
                ri.setItem(item);
                ri.setItemQuantity(qty);
                ri.setType(PERSISTENT);
                ri.setState(ISSUED);
                ri.setSource("PRODUCT_SKU." + schema + "." + productId);
                ri.setContext("product-sku." + originalTransactionId + "." + reward.itemId() + "." + i);

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

    public ProductSkuDao getProductSkuDao() {
        return productSkuDao;
    }

    @Inject
    public void setProductSkuDao(ProductSkuDao productSkuDao) {
        this.productSkuDao = productSkuDao;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
