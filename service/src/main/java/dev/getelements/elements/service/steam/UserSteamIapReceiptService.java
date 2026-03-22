package dev.getelements.elements.service.steam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.steam.SteamIapReceipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import dev.getelements.elements.sdk.service.steam.SteamIapReceiptService;
import dev.getelements.elements.sdk.service.steam.client.invoker.SteamIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.steam.client.model.SteamIapQueryTxnResponse;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class UserSteamIapReceiptService implements SteamIapReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(UserSteamIapReceiptService.class);

    private static final String STATUS_COMMITTED = "Committed";
    private static final String STATUS_APPROVED = "Approved";

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ReceiptDao receiptDao;

    private ApplicationConfigurationDao applicationConfigurationDao;

    private ObjectMapper objectMapper;

    private SteamIapReceiptRequestInvoker requestInvoker;

    private Provider<Transaction> transactionProvider;

    private ElementRegistry elementRegistry;

    private ProductBundleService productBundleService;

    @Override
    public Pagination<SteamIapReceipt> getSteamIapReceipts(final User user, final int offset, final int count) {
        final var receipts = getReceiptDao().getReceipts(user, offset, count, STEAM_IAP_SCHEME);
        final var steamReceipts = receipts.getObjects().stream().map(this::convertReceipt);
        return Pagination.from(steamReceipts);
    }

    @Override
    public SteamIapReceipt getSteamIapReceipt(final String orderId) {
        return convertReceipt(getReceiptDao().getReceipt(STEAM_IAP_SCHEME, orderId));
    }

    @Override
    public SteamIapReceipt getOrCreateSteamIapReceipt(final SteamIapReceipt steamIapReceipt) {

        final var receipt = new Receipt();
        receipt.setSchema(STEAM_IAP_SCHEME);
        receipt.setOriginalTransactionId(steamIapReceipt.getOrderId());
        receipt.setUser(user);
        receipt.setPurchaseTime(steamIapReceipt.getPurchaseTime());

        try {
            receipt.setBody(getObjectMapper().writeValueAsString(steamIapReceipt));
        } catch (JsonProcessingException e) {
            throw new InternalException("Unable to serialize Steam IAP receipt: " + e.getMessage());
        }

        final var createdReceipt = getTransactionProvider().get().performAndClose(tx -> {
            final var receiptDao = tx.getDao(ReceiptDao.class);
            return convertReceipt(receiptDao.createReceipt(receipt));
        });

        getElementRegistry().publish(Event.builder()
                .argument(createdReceipt)
                .named(STEAM_IAP_RECEIPT_CREATED)
                .build());

        return createdReceipt;
    }

    @Override
    public void deleteSteamIapReceipt(final String orderId) {
        final var receipt = getReceiptDao().getReceipt(STEAM_IAP_SCHEME, orderId);
        getReceiptDao().deleteReceipt(receipt.getId());
    }

    @Override
    public SteamIapReceipt verifyAndCreateSteamIapReceiptIfNeeded(final String orderId) {

        final var profile = getCurrentProfileSupplier().get();

        if (profile == null) {
            throw new NotFoundException("User has no profile.");
        }

        final var application = profile.getApplication();

        if (application == null) {
            throw new InvalidDataException("Profile is not associated with a valid application.");
        }

        final var config = getSteamApplicationConfiguration(application);
        final var publisherKey = config.getPublisherKey();
        final var appId = config.getAppId();

        final SteamIapQueryTxnResponse response;

        try {
            response = getRequestInvoker().invokeQueryTxn(publisherKey, appId, orderId);
        } catch (Exception e) {
            logger.error("Communication with the Steam API was not successful: ", e);
            throw new InternalException("Communication with the Steam API was not successful: " + e.getMessage());
        }

        if (!response.isOk()) {
            throw new InvalidDataException("Steam transaction query returned a non-OK result for order: " + orderId);
        }

        final var params = response.getResponse().getParams();
        final var status = params.getStatus();

        if (!STATUS_COMMITTED.equals(status) && !STATUS_APPROVED.equals(status)) {
            throw new InvalidDataException(
                    "Steam transaction status is not valid for reward issuance. Status: " + status);
        }

        final var lineItems = response.getResponse().getLineItems();
        if (lineItems == null || lineItems.getLineItemList() == null || lineItems.getLineItemList().isEmpty()) {
            throw new InvalidDataException("Steam transaction contains no line items for order: " + orderId);
        }

        final var firstItem = lineItems.getLineItemList().get(0);

        final var steamIapReceipt = new SteamIapReceipt();
        steamIapReceipt.setOrderId(orderId);
        steamIapReceipt.setTransactionId(params.getTransactionId());
        steamIapReceipt.setSteamId(params.getSteamId());
        steamIapReceipt.setAppId(appId);
        steamIapReceipt.setItemId(firstItem.getItemId());
        steamIapReceipt.setStatus(status);
        steamIapReceipt.setCurrency(params.getCurrency());
        steamIapReceipt.setUser(user);

        return getOrCreateSteamIapReceipt(steamIapReceipt);
    }

    @Override
    public List<RewardIssuance> getOrCreateRewardIssuances(final SteamIapReceipt steamIapReceipt) {
        return getProductBundleService().processVerifiedPurchase(
                STEAM_IAP_SCHEME,
                steamIapReceipt.getItemId(),
                steamIapReceipt.getOrderId());
    }

    private SteamApplicationConfiguration getSteamApplicationConfiguration(final Application application) {
        final var applicationId = application.getId();

        if (applicationId == null || applicationId.isEmpty()) {
            throw new InvalidDataException("Application ID associated with the profile is invalid.");
        }

        return getApplicationConfigurationDao()
                .getDefaultApplicationConfigurationForApplication(
                        applicationId,
                        SteamApplicationConfiguration.class
                );
    }

    private SteamIapReceipt convertReceipt(final Receipt receipt) {
        try {
            return getObjectMapper().readValue(receipt.getBody(), SteamIapReceipt.class);
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

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    public ReceiptDao getReceiptDao() {
        return receiptDao;
    }

    @Inject
    public void setReceiptDao(ReceiptDao receiptDao) {
        this.receiptDao = receiptDao;
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SteamIapReceiptRequestInvoker getRequestInvoker() {
        return requestInvoker;
    }

    @Inject
    public void setRequestInvoker(SteamIapReceiptRequestInvoker requestInvoker) {
        this.requestInvoker = requestInvoker;
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
