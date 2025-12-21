package dev.getelements.elements.service.appleiap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;

import static dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt.buildRewardIssuanceTags;

import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
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

    private Provider<Transaction> transactionProvider;

    private ElementRegistry elementRegistry;

    @Override
    public Pagination<AppleIapReceipt> getAppleIapReceipts(User user, int offset, int count) {
        final var receiptPagination = getReceiptDao().getReceipts(user, offset, count, APPLE_IAP_SCHEME);
        final var appleReceipts = receiptPagination.getObjects().stream().map(this::convertReceipt);
        return Pagination.from(appleReceipts);
    }

    @Override
    public AppleIapReceipt getAppleIapReceipt(String originalTransactionId) {
        final var receipt = getReceiptDao().getReceipt(APPLE_IAP_SCHEME, originalTransactionId);
        return convertReceipt(receipt);
    }

    @Override
    public AppleIapReceipt getOrCreateAppleIapReceipt(final AppleIapReceipt appleIapReceipt) {

        final var receipt = new Receipt();
        receipt.setSchema(APPLE_IAP_SCHEME);
        receipt.setOriginalTransactionId(appleIapReceipt.getOriginalTransactionId());
        receipt.setUser(user);
        receipt.setPurchaseTime(appleIapReceipt.getOriginalPurchaseDate().getTime());

        try {
            final String body = getObjectMapper().writeValueAsString(appleIapReceipt);
            receipt.setBody(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        final var createdReceipt = getTransactionProvider().get().performAndClose(tx -> {
            final var receiptDao = tx.getDao(ReceiptDao.class);
            return convertReceipt(receiptDao.createReceipt(receipt));
        });

        getElementRegistry().publish(Event.builder()
                .argument(createdReceipt)
                .named(APPLE_IAP_RECEIPT_CREATED)
                .build());

        return createdReceipt;
    }

    @Override
    public void deleteAppleIapReceipt(String originalTransactionId) {
        final var receipt = getReceiptDao().getReceipt(APPLE_IAP_SCHEME, originalTransactionId);
        getReceiptDao().deleteReceipt(receipt.getId());
    }

    @Override
    public List<AppleIapReceipt> verifyAndCreateAppleIapReceiptsIfNeeded(
            final AppleIapVerifyReceiptInvoker.AppleIapVerifyReceiptEnvironment appleIapVerifyReceiptEnvironment,
            final String receiptData) {

            final var appleIapGrandUnifiedReceipt =
                    getAppleIapVerifyReceiptInvokerBuilderProvider().get()
                            .withEnvironment(appleIapVerifyReceiptEnvironment)
                            .withReceiptData(receiptData)
                            .build()
                            .invoke();

            final var resultAppleIapReceipts = new ArrayList<AppleIapReceipt>();

            for (final var appleIapGrandUnifiedReceiptPurchase : appleIapGrandUnifiedReceipt.getInApp()) {
                // map the grand unified receipt and the purchase entities into a single model for db insertion
                final var appleIapReceipt = getDozerMapper().map(appleIapGrandUnifiedReceipt, AppleIapReceipt.class);
                getDozerMapper().map(appleIapGrandUnifiedReceiptPurchase, appleIapReceipt);

                // manually add the receipt data and user to the model
                appleIapReceipt.setReceiptData(receiptData);
                appleIapReceipt.setUser(getUser());

                // perform insertion/retrieval
                final var resultAppleIapReceipt = getOrCreateAppleIapReceipt(appleIapReceipt);

                // add to results
                resultAppleIapReceipts.add(resultAppleIapReceipt);
            }

            return resultAppleIapReceipts;
    }

    @Override
    public List<RewardIssuance> getOrCreateRewardIssuances(List<AppleIapReceipt> appleIapReceipts) {

        final var resultRewardIssuances = new ArrayList<RewardIssuance>();
        final var profile = getCurrentProfileSupplier().get();

        if (profile == null) {
            throw new NotFoundException("User has no profile.");
        }

        final var application = profile.getApplication();

        if (application == null) {
            throw new InvalidDataException("Profile is not associated with a valid application.");
        }

        final var applicationId = application.getId();

        if (applicationId == null || applicationId.isEmpty()) {
            throw new InvalidDataException("Application id associated with the profile is invalid.");
        }

        // next, we look up the associated application configuration
        final var iosApplicationConfiguration = getApplicationConfigurationDao()
                .getDefaultApplicationConfigurationForApplication(
                        applicationId,
                        IosApplicationConfiguration.class);

        return getTransactionProvider().get().performAndClose(tx ->
        {
            // for each purchase we received from the ios app...
            for (final var appleIapReceipt : appleIapReceipts) {
                final var productId = appleIapReceipt.getProductId();
                final var productBundle = iosApplicationConfiguration.getProductBundle(productId);

                if (productBundle == null) {
                    throw new InvalidDataException("ApplicationConfiguration " + iosApplicationConfiguration.getId() +
                            "has no ProductBundle for productId " + productId);
                }

                // for each reward in the product bundle...
                for (final var productBundleReward : productBundle.getProductBundleRewards()) {
                    // and for each instance of the SKU they purchased...
                    for (int skuOrdinal = 0; skuOrdinal < appleIapReceipt.getQuantity(); skuOrdinal++) {
                        final RewardIssuance resultRewardIssuance = getOrCreateRewardIssuance(
                                tx,
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
        });

    }

    private RewardIssuance getOrCreateRewardIssuance(
            final Transaction transaction,
            final String originalTransactionId,
            final String itemId,
            final Integer quantity,
            final Integer skuOrdinal
    ) {

        final var itemDao = transaction.getDao(ItemDao.class);
        final var rewardIssuanceDao = transaction.getDao(RewardIssuanceDao.class);
        final var context = buildAppleIapContextString(originalTransactionId, itemId, skuOrdinal);
        final var item = itemDao.getItemByIdOrName(itemId);
        final var metadata = generateAppleIapReceiptMetadata();
        final var rewardIssuance = new RewardIssuance();

        rewardIssuance.setItem(item);
        rewardIssuance.setItemQuantity(quantity);
        rewardIssuance.setUser(user);
        // we hold onto the reward issuance forever so as not to duplicate an already-redeemed issuance
        rewardIssuance.setType(PERSISTENT);
        rewardIssuance.setContext(context);
        rewardIssuance.setMetadata(metadata);
        rewardIssuance.setSource(APPLE_IAP_SOURCE);

        final var tags = buildRewardIssuanceTags(originalTransactionId, skuOrdinal);
        rewardIssuance.setTags(tags);

        return rewardIssuanceDao.getOrCreateRewardIssuance(rewardIssuance);
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


    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }
}
