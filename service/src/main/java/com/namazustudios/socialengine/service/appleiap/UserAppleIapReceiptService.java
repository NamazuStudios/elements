package com.namazustudios.socialengine.service.appleiap;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import com.namazustudios.socialengine.model.application.Application;

import static com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt.buildRewardIssuanceTags;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.IOS_APP_STORE;

import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment;
import com.namazustudios.socialengine.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import com.namazustudios.socialengine.service.appleiap.client.model.AppleIapGrandUnifiedReceiptPurchase;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.model.mission.RewardIssuance.APPLE_IAP_SOURCE;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.Type.PERSISTENT;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.buildAppleIapContextString;

public class UserAppleIapReceiptService implements AppleIapReceiptService {

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private AppleIapReceiptDao appleIapReceiptDao;

    private Provider<AppleIapVerifyReceiptInvoker.Builder> appleIapVerifyReceiptInvokerBuilderProvider;

    private Mapper dozerMapper;

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

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
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
            AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment,
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
                        IOS_APP_STORE,
                        IosApplicationConfiguration.class);

        // for each purchase we received from the ios app...
        for (AppleIapReceipt appleIapReceipt : appleIapReceipts) {
            // and for each instance of the SKU they purchased...
            for (int skuOrdinal = 0; skuOrdinal < appleIapReceipt.getQuantity(); skuOrdinal++) {
                final String context = buildAppleIapContextString(
                        appleIapReceipt.getOriginalTransactionId(),
                        skuOrdinal
                );

                final Map<String, Object> metadata = generateAppleIapReceiptMetadata();

                try {
                    final RewardIssuance resultRewardIssuance = getRewardIssuanceDao().getRewardIssuance(user, context);
                    resultRewardIssuances.add(resultRewardIssuance);
                } catch (NotFoundException e) {
                    // now, we look up the item id related to the current receipt's product id
                    final String productId = appleIapReceipt.getProductId();
                    final Map<String, String> iapProductIdsToItemIds = iosApplicationConfiguration
                            .getIapProductIdsToItemIds();

                    if (iapProductIdsToItemIds == null) {
                        throw new InvalidDataException("Application Configuration " + iosApplicationConfiguration.getId() +
                                "has no product id -> item id mapping.");
                    }
                    if (!iapProductIdsToItemIds.containsKey(productId)) {
                        throw new NotFoundException("IAP product id " + productId + " is not in the application " +
                                "configuration " + iosApplicationConfiguration.getId() + "  product id -> item id " +
                                "mapping.");
                    }

                    final String itemId = iapProductIdsToItemIds.get(productId);

                    // we also need to look up the reward quantity for the current receipt's product id
                    final Map<String, Integer> iapProductIdsToRewardQuantities = iosApplicationConfiguration
                            .getIapProductIdsToRewardQuantities();

                    if (iapProductIdsToRewardQuantities == null) {
                        throw new InvalidDataException("Application Configuration " + iosApplicationConfiguration.getId() +
                                "has no product id -> reward quantity mapping.");
                    }
                    if (!iapProductIdsToRewardQuantities.containsKey(productId)) {
                        throw new NotFoundException("IAP product id " + productId + " is not in the application " +
                                "configuration " + iosApplicationConfiguration.getId() + "  product id -> reward " +
                                "quantity mapping.");
                    }

                    final Integer rewardQuantity = iapProductIdsToRewardQuantities.get(productId);

                    // then, we get a model rep of the given item id
                    final Item item = getItemDao().getItemByIdOrName(itemId);

                    // we now have everything we need to set up and insert a new issuance...
                    final RewardIssuance rewardIssuance = new RewardIssuance();

                    rewardIssuance.setItem(item);
                    rewardIssuance.setItemQuantity(rewardQuantity);
                    rewardIssuance.setUser(user);
                    // we hold onto the reward issuance forever so as not to duplicate an already-redeemed issuance
                    rewardIssuance.setType(PERSISTENT);
                    rewardIssuance.setContext(context);
                    rewardIssuance.setMetadata(metadata);
                    rewardIssuance.setSource(APPLE_IAP_SOURCE);

                    final Set<String> tags =
                            buildRewardIssuanceTags(appleIapReceipt.getOriginalTransactionId(), skuOrdinal);
                    rewardIssuance.setTags(tags);

                    final RewardIssuance resultRewardIssuance = getRewardIssuanceDao()
                            .getOrCreateRewardIssuance(rewardIssuance);

                    // finally, we add the inserted issuance to the list of results
                    resultRewardIssuances.add(resultRewardIssuance);
                }
            }
        }

        return resultRewardIssuances;
    }

    public Map<String, Object> generateAppleIapReceiptMetadata() {
        final HashMap<String, Object> map = new HashMap<>();

        return map;
    }
}
