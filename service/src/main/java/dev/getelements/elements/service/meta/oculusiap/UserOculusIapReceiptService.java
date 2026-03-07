package dev.getelements.elements.service.meta.oculusiap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.service.goods.ProductSkuService;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.invoker.OculusIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;

import java.util.List;
import java.util.function.Supplier;

public class UserOculusIapReceiptService implements OculusIapReceiptService {

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ReceiptDao receiptDao;

    private MapperRegistry dozerMapperRegistry;

    private ApplicationConfigurationDao applicationConfigurationDao;

    private Client client;

    private ObjectMapper objectMapper;

    private OculusIapReceiptRequestInvoker requestInvoker;

    private Provider<Transaction> transactionProvider;

    private ElementRegistry elementRegistry;

    private ProductSkuService productSkuService;

    @Override
    public Pagination<OculusIapReceipt> getOculusIapReceipts(final int offset, final int count) {
        final var search = OCULUS_IAP_SCHEME;
        final var receipts = receiptDao.getReceipts(user, offset, count, search);
        final var fbReceipts = receipts.getObjects().stream().map(this::convertReceipt);
        return Pagination.from(fbReceipts);
    }

    @Override
    public OculusIapReceipt getOculusIapReceipt(final String originalTransactionId) {
        return convertReceipt(receiptDao.getReceipt(OCULUS_IAP_SCHEME, originalTransactionId));
    }

    @Override
    public OculusIapReceipt getOrCreateOculusIapReceipt(final OculusIapReceipt oculusIapReceipt) {

        final var receipt = new Receipt();

        try {
            final var body = getObjectMapper().writeValueAsString(oculusIapReceipt);
            receipt.setBody(body);
        } catch (JsonProcessingException e) {
            throw new InternalError("Unable to serialize receipt: " + e.getMessage());
        }

        receipt.setOriginalTransactionId(oculusIapReceipt.getPurchaseId());
        receipt.setSchema(OCULUS_IAP_SCHEME);

        final var createdReceipt = getTransactionProvider().get().performAndClose(tx -> {
            final var receiptDao = tx.getDao(ReceiptDao.class);
            return convertReceipt(receiptDao.createReceipt(receipt));
        });

        getElementRegistry().publish(Event.builder()
                .argument(createdReceipt)
                .named(OCULUS_IAP_RECEIPT_CREATED)
                .build());

        return createdReceipt;
    }

    @Override
    public void deleteOculusIapReceipt(final String transactionId) {
        final var receipt = receiptDao.getReceipt(OCULUS_IAP_SCHEME, transactionId);
        receiptDao.deleteReceipt(receipt.getId());
    }

    @Override
    public OculusIapVerifyReceiptResponse verifyAndCreateOculusIapReceiptIfNeeded(final OculusIapReceipt receiptData) {

        final var profile = getCurrentProfileSupplier().get();

        if (profile == null) {
            throw new NotFoundException("User has no profile.");
        }

        final var application = profile.getApplication();

        if (application == null) {
            throw new InvalidDataException("Profile is not associated with a valid application.");
        }

        final var applicationConfiguration = getOculusApplicationConfiguration(application);
        final var appId = applicationConfiguration.getApplicationId();
        final var appSecret = applicationConfiguration.getApplicationSecret();

        final var response = requestInvoker.invokeVerify(receiptData, appId, appSecret);

        // If verification was successful, we try to write the receipt to the db
        if(response != null && response.isSuccess()) {
            getOrCreateOculusIapReceipt(receiptData);
        }

        return response;
    }

    @Override
    public OculusIapConsumeResponse consumeAndRecordOculusIapReceipt(final OculusIapReceipt receiptData) {

        final var profile = getCurrentProfileSupplier().get();

        if (profile == null) {
            throw new NotFoundException("User has no profile.");
        }

        final var application = profile.getApplication();

        if (application == null) {
            throw new InvalidDataException("Profile is not associated with a valid application.");
        }

        final var applicationConfiguration = getOculusApplicationConfiguration(application);
        final var appId = applicationConfiguration.getApplicationId();
        final var appSecret = applicationConfiguration.getApplicationSecret();

        final var response = requestInvoker.invokeConsume(receiptData, appId, appSecret);

        // If consumption was successful, we try to write the receipt to the db and process rewards
        if(response.getSuccess()) {
            getOrCreateOculusIapReceipt(receiptData);
            getProductSkuService().processVerifiedPurchase(
                    OCULUS_IAP_SCHEME,
                    receiptData.getSku(),
                    receiptData.getPurchaseId());
        }

        return response;
    }

    @Override
    public List<RewardIssuance> getOrCreateRewardIssuances(final OculusIapReceipt oculusIapReceipt) {
        return List.of();
    }

    private OculusIapReceipt convertReceipt(final Receipt receipt) {
        try {
            return getObjectMapper().readValue(receipt.getBody(), OculusIapReceipt.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private OculusApplicationConfiguration getOculusApplicationConfiguration(final Application application) {

        final var applicationId = application.getId();

        if (applicationId == null || applicationId.isEmpty()) {
            throw new InvalidDataException("Application id associated with the profile is invalid.");
        }

        return getApplicationConfigurationDao()
                .getDefaultApplicationConfigurationForApplication(
                        applicationId,
                        OculusApplicationConfiguration.class
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

    public OculusIapReceiptRequestInvoker getRequestInvoker() {
        return requestInvoker;
    }

    @Inject
    public void setRequestInvoker(OculusIapReceiptRequestInvoker requestInvoker) {
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

    public ProductSkuService getProductSkuService() {
        return productSkuService;
    }

    @Inject
    public void setProductSkuService(ProductSkuService productSkuService) {
        this.productSkuService = productSkuService;
    }
}
