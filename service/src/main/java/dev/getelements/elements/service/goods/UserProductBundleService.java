package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.ProductBundleDao;
import dev.getelements.elements.sdk.dao.RewardIssuanceDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.ProductBundle;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.State.ISSUED;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.PERSISTENT;

public class UserProductBundleService implements ProductBundleService {

    private static final Logger logger = LoggerFactory.getLogger(UserProductBundleService.class);

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ProductBundleDao productBundleDao;

    private Provider<Transaction> transactionProvider;

    @Override
    public Pagination<ProductBundle> getProductBundles(final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(final String applicationNameOrId, final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(final String applicationNameOrId, final String schema,
                                                       final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public Pagination<ProductBundle> getProductBundles(final String applicationNameOrId, final String schema,
                                                       final String productId, final List<String> tags,
                                                       final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public Pagination<ProductBundle> getProductBundlesByTag(final String tag, final int offset, final int count) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public ProductBundle getProductBundle(final String id) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public ProductBundle getProductBundle(final String applicationNameOrId, final String schema, final String productId) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public ProductBundle createProductBundle(final ProductBundle bundle) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public ProductBundle updateProductBundle(final ProductBundle bundle) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public void deleteProductBundle(final String id) {
        throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
    }

    @Override
    public List<RewardIssuance> processVerifiedPurchase(
            final String schema,
            final String productId,
            final String originalTransactionId) {

        final var profile = getCurrentProfileSupplier().get();
        final var applicationId = profile.getApplication().getId();

        final ProductBundle bundle;

        try {
            bundle = getProductBundleDao().getProductBundle(applicationId, schema, productId);
        } catch (NotFoundException e) {
            logger.debug("No Product Bundle configured for application={} schema={} productId={}",
                    applicationId, schema, productId);
            return List.of();
        }

        return getTransactionProvider().get().performAndClose(tx -> {

            final var rewardIssuanceDao = tx.getDao(RewardIssuanceDao.class);
            final var itemDao = tx.getDao(ItemDao.class);
            final var rewards = bundle.getProductBundleRewards();

            if (rewards == null) return List.of();

            final var issuances = new ArrayList<RewardIssuance>();

            for (int i = 0; i < rewards.size(); i++) {

                final var reward = rewards.get(i);
                final var item = itemDao.getItemByIdOrName(reward.getItemId());
                final int qty = DISTINCT.equals(item.getCategory())
                        ? 1
                        : (reward.getQuantity() != null ? reward.getQuantity() : 1);

                final var rewardIssuance = new RewardIssuance();
                rewardIssuance.setUser(getUser());
                rewardIssuance.setItem(item);
                rewardIssuance.setItemQuantity(qty);
                rewardIssuance.setType(PERSISTENT);
                rewardIssuance.setState(ISSUED);
                rewardIssuance.setSource("PRODUCT_BUNDLE." + schema + "." + productId);
                rewardIssuance.setContext("product-bundle." + originalTransactionId + "." + reward.getItemId() + "." + i);

                issuances.add(rewardIssuanceDao.getOrCreateRewardIssuance(rewardIssuance));
            }

            return issuances;
        });
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    public ProductBundleDao getProductBundleDao() {
        return productBundleDao;
    }

    @Inject
    public void setProductBundleDao(ProductBundleDao productBundleDao) {
        this.productBundleDao = productBundleDao;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
