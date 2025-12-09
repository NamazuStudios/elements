package dev.getelements.elements.service.googleplayiap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.application.ProductBundleReward;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;

import static dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt.PURCHASE_STATE_CANCELED;
import static dev.getelements.elements.sdk.model.googleplayiapreceipt.GooglePlayIapReceipt.buildRewardIssuanceTags;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.*;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.PERSISTENT;
import static java.nio.charset.StandardCharsets.UTF_8;

public class UserGooglePlayIapReceiptService implements GooglePlayIapReceiptService {

    private static final Logger logger = LoggerFactory.getLogger(UserGooglePlayIapReceiptService.class);

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ReceiptDao receiptDao;

    private MapperRegistry dozerMapperRegistry;

    private final ObjectMapper objectMapper;
    {
        objectMapper = new ObjectMapper();
    }

    private RewardIssuanceDao rewardIssuanceDao;

    private ItemDao itemDao;

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public Pagination<GooglePlayIapReceipt> getGooglePlayIapReceipts(User user, int offset, int count) {
        final var receiptPagination = getReceiptDao().getReceipts(user, offset, count, GOOGLE_PLAY_IAP_SOURCE);
        final var googleReceipts = receiptPagination.getObjects().stream().map(this::convertReceipt);

        return Pagination.from(googleReceipts);
    }

    @Override
    public GooglePlayIapReceipt getGooglePlayIapReceipt(String orderId) {
        final var receipt = getReceiptDao().getReceipt(GOOGLE_PLAY_IAP_SOURCE, orderId);
        return convertReceipt(receipt);
    }

    @Override
    public GooglePlayIapReceipt getOrCreateGooglePlayIapReceipt(GooglePlayIapReceipt googlePlayIapReceipt) {
        final var receipt = new Receipt();
        receipt.setSchema(GOOGLE_PLAY_IAP_SOURCE);
        receipt.setOriginalTransactionId(googlePlayIapReceipt.getOrderId());
        receipt.setUser(user);
        receipt.setPurchaseTime(googlePlayIapReceipt.getPurchaseTimeMillis());

        final String body;
        try {
            body = getObjectMapper().writeValueAsString(googlePlayIapReceipt);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        receipt.setBody(body);

        final var createdReceipt = getReceiptDao().createReceipt(receipt);

        return convertReceipt(createdReceipt);
    }

    @Override
    public void deleteGooglePlayIapReceipt(String orderId) {
        getReceiptDao().deleteReceipt(orderId);
    }

    @Override
    public GooglePlayIapReceipt verifyAndCreateGooglePlayIapReceiptIfNeeded(
            String packageName,
            String productId,
            String purchaseToken
    ) {
        final String platformApplicationId = getCurrentPlatformApplicationId();

        final String jsonKeyString = getCurrentJsonKeyString();

        /**
         * The GoogleCredential class normally expects a json file provided via InputStream, so we convert the json
         * key string to also be an InputStream since we store the json data in the db and retrieve it to memory via
         * Morphia.
         */
        final InputStream jsonKeyStream = new ByteArrayInputStream(jsonKeyString.getBytes(UTF_8));

        final ProductPurchase productPurchase;

        try {
            final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
            final GoogleCredential googleCredential = GoogleCredential
                    .fromStream(jsonKeyStream)
                    .createScoped(
                            Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER)
                    );
            productPurchase = new AndroidPublisher
                    .Builder(httpTransport, jacksonFactory, googleCredential)
                    .setApplicationName(platformApplicationId)
                    .build()
                    .purchases()
                    .products()
                    .get(packageName, productId, purchaseToken)
                    .execute();
        }
        catch (Exception e) {
            logger.error("Communication with the Google Play services was not successful: ", e);
            throw new InternalException("Communication with the Google Play services was not successful:\n\n" + e);
        }

        final GooglePlayIapReceipt googlePlayIapReceipt =
                getDozerMapper().map(productPurchase, GooglePlayIapReceipt.class);

        googlePlayIapReceipt.setUser(getUser());
        googlePlayIapReceipt.setPurchaseToken(purchaseToken);
        googlePlayIapReceipt.setProductId(productId);

        final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                getOrCreateGooglePlayIapReceipt(googlePlayIapReceipt);

        return resultGooglePlayIapReceipt;
    }

    @Override
    public List<RewardIssuance> getOrCreateRewardIssuances(GooglePlayIapReceipt googlePlayIapReceipt) {
        if (PURCHASE_STATE_CANCELED == googlePlayIapReceipt.getPurchaseState()) {
            throw new InvalidDataException("Google Play purchase marked as canceled in purchaseState.");
        }

        GooglePlayApplicationConfiguration googlePlayApplicationConfiguration =
                getGooglePlayApplicationConfiguration();

        final String productId = googlePlayIapReceipt.getProductId();

        final ProductBundle productBundle = googlePlayApplicationConfiguration.getProductBundle(productId);

        final List<RewardIssuance> rewardIssuances = new ArrayList<>();

        for (ProductBundleReward productBundleReward : productBundle.getProductBundleRewards()) {
            final RewardIssuance resultRewardIssuance = getOrCreateRewardIssuance(
                    googlePlayIapReceipt.getOrderId(),
                    productBundleReward.getItemId(),
                    productBundleReward.getQuantity());
            rewardIssuances.add(resultRewardIssuance);
        }

        return rewardIssuances;
    }

    private RewardIssuance getOrCreateRewardIssuance(String orderId, String itemId, Integer quantity) {
        final String context = buildGooglePlayIapContextString(orderId, itemId);

        final Map<String, Object> metadata = generateGooglePlayIapReceiptMetadata();
        final List<String> tags = buildRewardIssuanceTags(orderId);

        final Item item = getItemDao().getItemByIdOrName(itemId);

        final RewardIssuance rewardIssuance = new RewardIssuance();

        rewardIssuance.setItem(item);
        rewardIssuance.setItemQuantity(quantity);
        rewardIssuance.setUser(user);
        // we hold onto the reward issuance forever so as not to duplicate an already-redeemed issuance
        rewardIssuance.setType(PERSISTENT);
        rewardIssuance.setContext(context);
        rewardIssuance.setTags(tags);
        rewardIssuance.setMetadata(metadata);
        rewardIssuance.setSource(GOOGLE_PLAY_IAP_SOURCE);


        final RewardIssuance resultRewardIssuance = getRewardIssuanceDao()
                .getOrCreateRewardIssuance(rewardIssuance);

        return resultRewardIssuance;
    }

    public Map<String, Object> generateGooglePlayIapReceiptMetadata() {
        final HashMap<String, Object> map = new HashMap<>();

        return map;
    }

    private GooglePlayApplicationConfiguration getGooglePlayApplicationConfiguration() {
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

        GooglePlayApplicationConfiguration googlePlayApplicationConfiguration = getApplicationConfigurationDao()
                .getDefaultApplicationConfigurationForApplication(
                        applicationId,
                        GooglePlayApplicationConfiguration.class
                );

        return googlePlayApplicationConfiguration;
    }

    private String getCurrentPlatformApplicationId() {
        GooglePlayApplicationConfiguration googlePlayApplicationConfiguration = getGooglePlayApplicationConfiguration();

        final String platformApplicationId;

        if (googlePlayApplicationConfiguration.getApplicationId() != null) {
            platformApplicationId = googlePlayApplicationConfiguration.getApplicationId();
        } else if (googlePlayApplicationConfiguration.getName() != null) {
            platformApplicationId = googlePlayApplicationConfiguration.getName();
        } else {
            throw new InvalidDataException("Platform application id (applicationId or uniqueIdentifier) is not set " +
                    "in application configuration " + googlePlayApplicationConfiguration.getId());
        }

        return platformApplicationId;
    }

    private String getCurrentJsonKeyString() {
        GooglePlayApplicationConfiguration googlePlayApplicationConfiguration = getGooglePlayApplicationConfiguration();

        final Map<String, Object> jsonKey = googlePlayApplicationConfiguration.getJsonKey();

        if (jsonKey == null) {
            throw new InvalidDataException("JsonKey is not set in application configuration " +
                    googlePlayApplicationConfiguration.getId());
        }

        final String jsonKeyString;
        try {
            jsonKeyString = getObjectMapper().writeValueAsString(jsonKey);
        }
        catch (JsonProcessingException e) {
            throw new InvalidDataException("Could not convert jsonKey to string for application configuration: " +
                    googlePlayApplicationConfiguration.getId());
        }

        return jsonKeyString;
    }

    private GooglePlayIapReceipt convertReceipt(Receipt receipt) {
        try {
            return getObjectMapper().readValue(receipt.getBody(), GooglePlayIapReceipt.class);
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

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
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
}
