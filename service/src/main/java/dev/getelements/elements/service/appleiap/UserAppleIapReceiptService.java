package dev.getelements.elements.service.appleiap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.receipt.Receipt;
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

    private ReceiptDao receiptDao;

    private Provider<AppleIapVerifyReceiptInvoker.Builder> appleIapVerifyReceiptInvokerBuilderProvider;

    private MapperRegistry dozerMapperRegistry;

    private RewardIssuanceDao rewardIssuanceDao;

    private ItemDao itemDao;

    private ApplicationConfigurationDao applicationConfigurationDao;

    private ObjectMapper objectMapper;

    @Override
    public Pagination<AppleIapReceipt> getAppleIapReceipts(User user, int offset, int count) {
        final var receiptPagination = getReceiptDao().getReceipts(user, offset, count, APPLE_IAP_SOURCE);
        final var appleReceipts = receiptPagination.getObjects().stream().map(this::convertReceipt);

        return Pagination.from(appleReceipts);
    }

    @Override
    public AppleIapReceipt getAppleIapReceipt(String originalTransactionId) {
        final var receipt = getReceiptDao().getReceipt(APPLE_IAP_SOURCE, originalTransactionId);
        return convertReceipt(receipt);
    }

    @Override
    public AppleIapReceipt getOrCreateAppleIapReceipt(AppleIapReceipt appleIapReceipt) {

        final var receipt = new Receipt();
        receipt.setSchema(APPLE_IAP_SOURCE);
        receipt.setOriginalTransactionId(appleIapReceipt.getOriginalTransactionId());
        receipt.setUser(user);
        receipt.setPurchaseTime(appleIapReceipt.getOriginalPurchaseDate().getTime());

        final String body;
        try {
            body = getObjectMapper().writeValueAsString(appleIapReceipt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        receipt.setBody(body);

        final var createdReceipt = getReceiptDao().createReceipt(receipt);

        return convertReceipt(createdReceipt);
    }

    @Override
    public void deleteAppleIapReceipt(String originalTransactionId) {
        getReceiptDao().deleteReceipt(originalTransactionId);
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

    private AppleIapReceipt convertReceipt(Receipt receipt) {
        try {
            return getObjectMapper().readValue(receipt.getBody(), AppleIapReceipt.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public ReceiptDao getReceiptDao() {
        return receiptDao;
    }

    @Inject
    public void setReceiptDao(ReceiptDao receiptDao) {
        this.receiptDao = receiptDao;
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


    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
