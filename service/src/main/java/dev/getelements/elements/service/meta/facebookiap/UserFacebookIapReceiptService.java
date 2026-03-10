package dev.getelements.elements.service.meta.facebookiap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.*;
import dev.getelements.elements.sdk.model.meta.facebookiapreceipt.FacebookIapReceipt;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import dev.getelements.elements.sdk.service.meta.facebookiap.FacebookIapReceiptService;
import dev.getelements.elements.sdk.service.meta.facebookiap.client.invoker.FacebookIapReceiptRequestInvoker;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;

import java.util.List;
import java.util.function.Supplier;

public class UserFacebookIapReceiptService implements FacebookIapReceiptService {

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ReceiptDao receiptDao;

    private MapperRegistry dozerMapperRegistry;

    private ApplicationConfigurationDao applicationConfigurationDao;

    private Client client;

    private ObjectMapper objectMapper;

    private FacebookIapReceiptRequestInvoker requestInvoker;

    private Provider<Transaction> transactionProvider;

    private ElementRegistry elementRegistry;

    private ProductBundleService productBundleService;

    @Override
    public Pagination<FacebookIapReceipt> getFacebookIapReceipts(final int offset, final int count) {
        final var search = FACEBOOK_IAP_SCHEME;
        final var receipts = receiptDao.getReceipts(user, offset, count, search);
        final var fbReceipts = receipts.getObjects().stream().map(this::convertReceipt);

        return Pagination.from(fbReceipts);
    }

    @Override
    public FacebookIapReceipt getFacebookIapReceipt(final String originalTransactionId) {
        return convertReceipt(receiptDao.getReceipt(FACEBOOK_IAP_SCHEME, originalTransactionId));
    }

    @Override
    public FacebookIapReceipt getOrCreateFacebookIapReceipt(final FacebookIapReceipt facebookIapReceipt) {

        final var receipt = new Receipt();

        try {
            final var body = getObjectMapper().writeValueAsString(facebookIapReceipt);
            receipt.setBody(body);
        } catch (JsonProcessingException e) {
            throw new InternalError("Unable to serialize receipt: " + e.getMessage());
        }

        receipt.setOriginalTransactionId(facebookIapReceipt.getPurchaseId());
        receipt.setSchema(FACEBOOK_IAP_SCHEME);

        final var createdReceipt = getTransactionProvider().get().performAndClose(tx -> {
            final var receiptDao = tx.getDao(ReceiptDao.class);
            return convertReceipt(receiptDao.createReceipt(receipt));
        });

        getElementRegistry().publish(Event.builder()
                .argument(createdReceipt)
                .named(FACEBOOK_IAP_RECEIPT_CREATED)
                .build());

        return createdReceipt;
    }

    @Override
    public void deleteFacebookIapReceipt(final String transactionId) {
        final var receipt = receiptDao.getReceipt(FACEBOOK_IAP_SCHEME, transactionId);
        receiptDao.deleteReceipt(receipt.getId());
    }

    @Override
    public List<RewardIssuance> verifyAndCreateFacebookIapReceiptIfNeeded(final FacebookIapReceipt receiptData) {

        final var profile = getCurrentProfileSupplier().get();

        if (profile == null) {
            throw new NotFoundException("User has no profile.");
        }

        final var application = profile.getApplication();

        if (application == null) {
            throw new InvalidDataException("Profile is not associated with a valid application.");
        }

        final var applicationConfiguration = getFacebookApplicationConfiguration(application);
        final var appId = applicationConfiguration.getApplicationId();
        final var appSecret = applicationConfiguration.getApplicationSecret();

        final var response = requestInvoker.invokeVerify(receiptData, appId, appSecret);

        // If verification was successful, we try to write the receipt to the db
        if(response != null && response.getItems() != null) {
            getOrCreateFacebookIapReceipt(receiptData);
            return getProductBundleService().processVerifiedPurchase(
                    FACEBOOK_IAP_SCHEME,
                    receiptData.getSku(),
                    receiptData.getPurchaseId());
        }

        return List.of();
    }

    private FacebookIapReceipt convertReceipt(final Receipt receipt) {
        try {
            return getObjectMapper().readValue(receipt.getBody(), FacebookIapReceipt.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private FacebookApplicationConfiguration getFacebookApplicationConfiguration(final Application application) {

        final var applicationId = application.getId();

        if (applicationId == null || applicationId.isEmpty()) {
            throw new InvalidDataException("Application id associated with the profile is invalid.");
        }

        return getApplicationConfigurationDao()
                .getDefaultApplicationConfigurationForApplication(
                        applicationId,
                        FacebookApplicationConfiguration.class
                );
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

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
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

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FacebookIapReceiptRequestInvoker getRequestInvoker() {
        return requestInvoker;
    }

    @Inject
    public void setRequestInvoker(FacebookIapReceiptRequestInvoker requestInvoker) {
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
