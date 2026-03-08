package dev.getelements.elements.service.appleiap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.appleiapreceipt.AppleIapReceipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.service.appleiap.AppleIapReceiptService;
import dev.getelements.elements.sdk.service.appleiap.client.invoker.AppleIapVerifyReceiptInvoker;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.*;
import java.util.function.Supplier;

public class UserAppleIapReceiptService implements AppleIapReceiptService {

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ReceiptDao receiptDao;

    private Provider<AppleIapVerifyReceiptInvoker.Builder> appleIapVerifyReceiptInvokerBuilderProvider;

    private MapperRegistry dozerMapperRegistry;

    private ObjectMapper objectMapper;

    private Provider<Transaction> transactionProvider;

    private ElementRegistry elementRegistry;

    private ProductBundleService productBundleService;

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

                // issue ProductBundle rewards if configured for this product
                getProductBundleService().processVerifiedPurchase(
                        APPLE_IAP_SCHEME,
                        appleIapReceipt.getProductId(),
                        appleIapReceipt.getOriginalTransactionId());

                // add to results
                resultAppleIapReceipts.add(resultAppleIapReceipt);
            }

            return resultAppleIapReceipts;
    }

    @Override
    public List<RewardIssuance> getOrCreateRewardIssuances(List<AppleIapReceipt> appleIapReceipts) {
        return List.of();
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

    public ProductBundleService getProductBundleService() {
        return productBundleService;
    }

    @Inject
    public void setProductBundleService(ProductBundleService productBundleService) {
        this.productBundleService = productBundleService;
    }
}
