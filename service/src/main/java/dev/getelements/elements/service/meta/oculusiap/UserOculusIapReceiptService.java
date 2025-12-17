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
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.meta.oculusiap.OculusIapReceiptService;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.invoker.OculusIapReceiptRequestInvoker;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapConsumeResponse;
import dev.getelements.elements.sdk.service.meta.oculusiap.client.model.OculusIapVerifyReceiptResponse;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.getelements.elements.sdk.model.meta.oculusiapreceipt.OculusIapReceipt.buildRewardIssuanceTags;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.*;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.PERSISTENT;

public class UserOculusIapReceiptService implements OculusIapReceiptService {

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ReceiptDao receiptDao;

    private MapperRegistry dozerMapperRegistry;

    private RewardIssuanceDao rewardIssuanceDao;

    private ItemDao itemDao;

    private ApplicationConfigurationDao applicationConfigurationDao;

    private Client client;

    private ObjectMapper objectMapper;

    private OculusIapReceiptRequestInvoker requestInvoker;

    private Provider<Transaction> transactionProvider;

    private ElementRegistry elementRegistry;

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

        return getTransactionProvider().get().performAndClose(tx -> {
            final var receiptDao = tx.getDao(ReceiptDao.class);
            final var convertedReceipt = convertReceipt(receiptDao.createReceipt(receipt));

            getElementRegistry().publish(Event.builder()
                    .argument(convertedReceipt)
                    .named(OCULUS_IAP_RECEIPT_CREATED)
                    .build());

            return convertedReceipt;
        });
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
            getOrCreateRewardIssuances(receiptData);
        }

        return response;
    }

    @Override
    public List<RewardIssuance> getOrCreateRewardIssuances(final OculusIapReceipt oculusIapReceipt) {

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

        return getTransactionProvider().get().performAndClose( tx -> {

            // next, we look up the associated application configuration
            final var applicationConfigurationDao = tx.getDao(ApplicationConfigurationDao.class);
            final var oculusApplicationConfiguration = applicationConfigurationDao
                    .getDefaultApplicationConfigurationForApplication(
                            applicationId,
                            OculusApplicationConfiguration.class);

            final var productBundles = oculusApplicationConfiguration.getProductBundles();
            final var productId = oculusIapReceipt.getSku();
            final var productBundle = productBundles.stream()
                    .filter(p -> p.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (productBundle == null) {
                throw new InvalidDataException("ApplicationConfiguration " + oculusApplicationConfiguration.getId() +
                        "has no ProductBundle for productId " + productId);
            }

            final var itemDao = tx.getDao(ItemDao.class);
            final var rewardIssuanceDao = tx.getDao(RewardIssuanceDao.class);

            // for each reward in the product bundle...
            for (final var productBundleReward : productBundle.getProductBundleRewards()) {

                final var item = itemDao.getItemByIdOrName(productBundleReward.getItemId());

                final var rewardIssuance = createRewardIssuance(
                        oculusIapReceipt.getPurchaseId(),
                        item,
                        productBundleReward.getQuantity()
                );

                final var resultRewardIssuance = rewardIssuanceDao.getOrCreateRewardIssuance(rewardIssuance);
                resultRewardIssuances.add(resultRewardIssuance);
            }

            return resultRewardIssuances;
        });

    }

    private RewardIssuance createRewardIssuance(
            final String originalTransactionId,
            final Item item,
            final Integer quantity
    ) {

        final var context = buildOculusIapContextString(originalTransactionId, item.getId());
        final var metadata = generateOculusIapReceiptMetadata();
        final var rewardIssuance = new RewardIssuance();

        rewardIssuance.setItem(item);
        rewardIssuance.setItemQuantity(quantity);
        rewardIssuance.setUser(user);
        // we hold onto the reward issuance forever so as not to duplicate an already-redeemed issuance
        rewardIssuance.setType(PERSISTENT);
        rewardIssuance.setContext(context);
        rewardIssuance.setMetadata(metadata);
        rewardIssuance.setSource(OCULUS_IAP_SOURCE);

        final var tags = buildRewardIssuanceTags(originalTransactionId);
        rewardIssuance.setTags(tags);

        return rewardIssuance;
    }

    private Map<String, Object> generateOculusIapReceiptMetadata() {
        final HashMap<String, Object> map = new HashMap<>();

        return map;
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
}
