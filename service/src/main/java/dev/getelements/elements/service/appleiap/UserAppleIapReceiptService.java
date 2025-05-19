package dev.getelements.elements.service.appleiap;

import dev.getelements.elements.sdk.dao.AppleIapReceiptDao;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.dao.RewardIssuanceDao;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.application.Application;

import static dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt.buildRewardIssuanceTags;

import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.application.ProductBundleReward;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import dev.getelements.elements.sdk.service.appleiap.client.model.AppleIapGrandUnifiedReceiptPurchase;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.*;
import java.util.function.Supplier;

import static dev.getelements.elements.sdk.model.reward.RewardIssuance.APPLE_IAP_SOURCE;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.PERSISTENT;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.buildAppleIapContextString;

public class UserAppleIapReceiptService implements AppleIapReceiptService {

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private AppleIapReceiptDao appleIapReceiptDao;

    private Provider<AppleIapVerifyReceiptInvoker.Builder> appleIapVerifyReceiptInvokerBuilderProvider;

    private MapperRegistry dozerMapperRegistry;

    private RewardIssuanceDao rewardIssuanceDao;

    private ItemDao itemDao;

    private ApplicationConfigurationDao applicationConfigurationDao;

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public AppleIapReceiptDao getAppleIapReceiptDao() {
        return appleIapReceiptDao;
    }

    @Inject
    public void setAppleIapReceiptDao(AppleIapReceiptDao appleIapReceiptDao) {
        this.appleIapReceiptDao = appleIapReceiptDao;
    }

    public Provider<AppleIapVerifyReceiptInvoker.Builder> getAppleIapVerifyReceiptInvokerBuilderProvider() {
        return appleIapVerifyReceiptInvokerBuilderProvider;
    }

    @Inject
    public void setAppleIapVerifyReceiptInvokerBuilderProvider(Provider<AppleIapVerifyReceiptInvoker.Builder> appleIapVerifyReceiptInvokerBuilderProvider) {
        this.appleIapVerifyReceiptInvokerBuilderProvider = appleIapVerifyReceiptInvokerBuilderProvider;
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }

    public RewardIssuanceDao getRewardIssuanceDao() {
        return rewardIssuanceDao;
    }

    @Inject
    public void setRewardIssuanceDao(RewardIssuanceDao rewardIssuanceDao) {
        this.rewardIssuanceDao = rewardIssuanceDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    @Override
    public Pagination<AppleIapReceipt> getAppleIapReceipts(User user, int offset, int count) {
        return appleIapReceiptDao.getAppleIapReceipts(user, offset, count);
    }

    @Override
    public AppleIapReceipt getAppleIapReceipt(String originalTransactionId) {
        return appleIapReceiptDao.getAppleIapReceipt(originalTransactionId);
    }

    @Override
    public AppleIapReceipt getOrCreateAppleIapReceipt(AppleIapReceipt appleIapReceipt) {
        return appleIapReceiptDao.getOrCreateAppleIapReceipt(appleIapReceipt);
    }

    @Override
    public void deleteAppleIapReceipt(String originalTransactionId) {
        appleIapReceiptDao.deleteAppleIapReceipt(originalTransactionId);
    }

    @Override
    public List<AppleIapReceipt> verifyAndCreateAppleIapReceiptsIfNeeded(
            AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment,
            String receiptData) {
            final AppleIapGrandUnifiedReceipt appleIapGrandUnifiedReceipt =
                    getAppleIapVerifyReceiptInvokerBuilderProvider().get()
                            .withEnvironment(appleIapVerifyReceiptEnvironment)
                            .withReceiptData(receiptData)
                            .build()
                            .invoke();

            final List<AppleIapReceipt> resultAppleIapReceipts = new ArrayList<>();

            for (final AppleIapGrandUnifiedReceiptPurchase appleIapGrandUnifiedReceiptPurchase :
                    appleIapGrandUnifiedReceipt.getInApp()) {
                // map the grand unified receipt and the purchase entities into a single model for db insertion
                final AppleIapReceipt appleIapReceipt = getDozerMapper().map(appleIapGrandUnifiedReceipt, AppleIapReceipt.class);
                getDozerMapper().map(appleIapGrandUnifiedReceiptPurchase, appleIapReceipt);

                // manually add the receipt data and user to the model
                appleIapReceipt.setReceiptData(receiptData);
                appleIapReceipt.setUser(getUser());

                // perform insertion/retrieval
                final AppleIapReceipt resultAppleIapReceipt = getOrCreateAppleIapReceipt(appleIapReceipt);

                // add to results
                resultAppleIapReceipts.add(resultAppleIapReceipt);
            }

            return resultAppleIapReceipts;
    }

    @Override
    public List<RewardIssuance> getOrCreateRewardIssuances(List<AppleIapReceipt> appleIapReceipts) {
        final List<RewardIssuance> resultRewardIssuances = new ArrayList<>();

        final Profile profile = getCurrentProfileSupplier().get();

        if (profile == null) {
            throw new NotFoundException("User has no profile.");
        }

        final Application application = profile.getApplication();

        if (application == null) {
            throw new InvalidDataException("Profile is not associated with a valid application.");
        }

        final String applicationId = application.getId();

        if (applicationId == null || applicationId.length() == 0) {
            throw new InvalidDataException("Application id associated with the profile is invalid.");
        }

        // next, we look up the associated application configuration
        IosApplicationConfiguration iosApplicationConfiguration =  getApplicationConfigurationDao()
                .getDefaultApplicationConfigurationForApplication(
                        applicationId,
                        IosApplicationConfiguration.class);

        // for each purchase we received from the ios app...
        for (final AppleIapReceipt appleIapReceipt : appleIapReceipts) {
            final String productId = appleIapReceipt.getProductId();
            final ProductBundle productBundle = iosApplicationConfiguration.getProductBundle(productId);

            if (productBundle == null) {
                throw new InvalidDataException("ApplicationConfiguration " + iosApplicationConfiguration.getId() +
                        "has no ProductBundle for productId " + productId);
            }

            // for each reward in the product bundle...
            for (final ProductBundleReward productBundleReward : productBundle.getProductBundleRewards()) {
                // and for each instance of the SKU they purchased...
                for (int skuOrdinal = 0; skuOrdinal < appleIapReceipt.getQuantity(); skuOrdinal++) {
                    final RewardIssuance resultRewardIssuance = getOrCreateRewardIssuance(
                            appleIapReceipt.getOriginalTransactionId(),
                            productBundleReward.getItemId(),
                            productBundleReward.getQuantity(),
                            skuOrdinal
                    );

                    resultRewardIssuances.add(resultRewardIssuance);
                }
            }
        }

        return resultRewardIssuances;
    }

    private RewardIssuance getOrCreateRewardIssuance(
            String originalTransactionId,
            String itemId,
            Integer quantity,
            Integer skuOrdinal
    ) {
        final String context = buildAppleIapContextString(originalTransactionId, itemId, skuOrdinal);

        final Item item = getItemDao().getItemByIdOrName(itemId);

        final Map<String, Object> metadata = generateAppleIapReceiptMetadata();

        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setItem(item);
        rewardIssuance.setItemQuantity(quantity);
        rewardIssuance.setUser(user);
        // we hold onto the reward issuance forever so as not to duplicate an already-redeemed issuance
        rewardIssuance.setType(PERSISTENT);
        rewardIssuance.setContext(context);
        rewardIssuance.setMetadata(metadata);
        rewardIssuance.setSource(APPLE_IAP_SOURCE);

        final List<String> tags = buildRewardIssuanceTags(originalTransactionId, skuOrdinal);
        rewardIssuance.setTags(tags);

        final RewardIssuance resultRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(rewardIssuance);

        return resultRewardIssuance;
    }


    public Map<String, Object> generateAppleIapReceiptMetadata() {
        final HashMap<String, Object> map = new HashMap<>();

        return map;
    }
}
