package com.namazustudios.socialengine.service.appleiap;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.IOS_APP_STORE;

import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import com.namazustudios.socialengine.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment;
import com.namazustudios.socialengine.service.appleiap.client.model.AppleIapGrandUnifiedReceipt;
import com.namazustudios.socialengine.service.appleiap.client.model.AppleIapGrandUnifiedReceiptPurchase;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.namazustudios.socialengine.model.mission.RewardIssuance.APPLE_IAP_SOURCE;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.Type.PERSISTENT;
import static com.namazustudios.socialengine.model.mission.RewardIssuance.buildAppleIapContextString;

public class UserAppleIapReceiptService implements AppleIapReceiptService {

    private Session session;

    private User user;

    private AppleIapReceiptDao appleIapReceiptDao;

    private Provider<AppleIapVerifyReceiptInvoker.Builder> appleIapVerifyReceiptInvokerBuilderProvider;

    private Mapper dozerMapper;

    private RewardIssuanceDao rewardIssuanceDao;

    private RewardDao rewardDao;

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

    public RewardDao getRewardDao() {
        return rewardDao;
    }

    @Inject
    public void setRewardDao(RewardDao rewardDao) {
        this.rewardDao = rewardDao;
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

    public Session getSession() {
        return session;
    }

    @Inject
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public Pagination<AppleIapReceipt> getAppleIapReceipts(User user, int offset, int count) {
        return appleIapReceiptDao.getAppleIapReceipts(user, offset, count);
    }

    @Override
    public AppleIapReceipt getAppleIapReceipt(String originalTransactionIdentifier) {
        return appleIapReceiptDao.getAppleIapReceipt(originalTransactionIdentifier);
    }

    @Override
    public AppleIapReceipt getOrCreateAppleIapReceipt(AppleIapReceipt appleIapReceipt) {
        return appleIapReceiptDao.getOrCreateAppleIapReceipt(appleIapReceipt);
    }

    @Override
    public void deleteAppleIapReceipt(String originalTransactionIdentifier) {
        appleIapReceiptDao.deleteAppleIapReceipt(originalTransactionIdentifier);
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
                getDozerMapper().map(appleIapReceipt, appleIapGrandUnifiedReceiptPurchase);

                // manually add the receipt data to the model
                appleIapReceipt.setReceiptData(receiptData);

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

        for (AppleIapReceipt appleIapReceipt : appleIapReceipts) {
            final String context = buildAppleIapContextString(appleIapReceipt.getOriginalTransactionIdentifier());
            final Map<String, Object> metadata = generateMissionProgressMetadata();

            try {
                final RewardIssuance resultRewardIssuance = getRewardIssuanceDao().getRewardIssuance(user, context);
                resultRewardIssuances.add(resultRewardIssuance);
            }
            catch (NotFoundException e) {
                // first, we get the application configuration based on the current session's application id
                final String applicationId = getSession().getApplication().getId();
                IosApplicationConfiguration iosApplicationConfiguration =  getApplicationConfigurationDao()
                        .getDefaultApplicationConfigurationForApplication(
                                applicationId,
                                IOS_APP_STORE,
                                IosApplicationConfiguration.class);

                // now, we look up the item id related to the current receipt's product id
                final String productId = appleIapReceipt.getProductIdentifier();
                final Map<String, String> iapProductIdsToItemIds = iosApplicationConfiguration
                        .getIapProductIdToItemIds();

                if (iapProductIdsToItemIds == null) {
                    throw new InvalidDataException("Application Configuration " + iosApplicationConfiguration.getId() +
                            "has no product id -> item id mapping.");
                }
                if (!iapProductIdsToItemIds.containsKey(productId)) {
                    throw new NotFoundException("IAP product id " + productId + " is not in the application " +
                            "configuration " + iosApplicationConfiguration.getId() +  "  product id -> item id " +
                            "mapping.");
                }

                final String itemId = iapProductIdsToItemIds.get(productId);

                // then, we get a model rep of the given item id
                final Item item = getItemDao().getItemByIdOrName(itemId);

                // we now have everything we need to set up and insert a new reward...
                final Reward reward = new Reward();

                final Integer quantity = appleIapReceipt.getQuantity();
                reward.setQuantity(quantity);
                reward.setItem(item);

                final Reward resultReward = getRewardDao().createReward(reward);

                // once the reward is inserted, we now have everything we need to set up and insert a new issuance...
                final RewardIssuance rewardIssuance = new RewardIssuance();

                rewardIssuance.setReward(resultReward);
                rewardIssuance.setUser(user);
                rewardIssuance.setType(PERSISTENT);
                rewardIssuance.setContext(context);
                rewardIssuance.setMetadata(metadata);
                rewardIssuance.setSource(APPLE_IAP_SOURCE);

                final RewardIssuance resultRewardIssuance = getRewardIssuanceDao()
                        .getOrCreateRewardIssuance(rewardIssuance);

                // finally, we add the inserted issuance to the list of results
                resultRewardIssuances.add(resultRewardIssuance);
            }
        }

        return resultRewardIssuances;
    }

    public Map<String, Object> generateMissionProgressMetadata() {
        final HashMap<String, Object> map = new HashMap<>();

        return map;
    }
}
