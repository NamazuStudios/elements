package com.namazustudios.socialengine.service.googleplayiap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.ProductPurchase;
import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.ANDROID_GOOGLE_PLAY;

import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.model.goods.Item;
import com.namazustudios.socialengine.model.googleplayiapreceipt.GooglePlayIapReceipt;
import com.namazustudios.socialengine.model.reward.Reward;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import com.namazustudios.socialengine.model.profile.Profile;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.namazustudios.socialengine.model.googleplayiapreceipt.GooglePlayIapReceipt.PURCHASE_STATE_CANCELED;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.*;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.model.reward.RewardIssuance.GOOGLE_PLAY_IAP_SOURCE;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.Type.PERSISTENT;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.buildGooglePlayIapContextString;

public class UserGooglePlayIapReceiptService implements GooglePlayIapReceiptService {
    private static final Logger logger = LoggerFactory.getLogger(UserGooglePlayIapReceiptService.class);

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private GooglePlayIapReceiptDao googlePlayIapReceiptDao;

    //private Provider<AppleIapVerifyReceiptInvoker.Builder> appleIapVerifyReceiptInvokerBuilderProvider;

    private Mapper dozerMapper;

    private final ObjectMapper objectMapper;
    {
        objectMapper = new ObjectMapper();
    }

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

    public GooglePlayIapReceiptDao getGooglePlayIapReceiptDao() {
        return googlePlayIapReceiptDao;
    }

    @Inject
    public void setGooglePlayIapReceiptDao(GooglePlayIapReceiptDao googlePlayIapReceiptDao) {
        this.googlePlayIapReceiptDao = googlePlayIapReceiptDao;
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
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

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    @Override
    public Pagination<GooglePlayIapReceipt> getGooglePlayIapReceipts(User user, int offset, int count) {
        return getGooglePlayIapReceiptDao().getGooglePlayIapReceipts(user, offset, count);
    }

    @Override
    public GooglePlayIapReceipt getGooglePlayIapReceipt(String orderId) {
        return getGooglePlayIapReceiptDao().getGooglePlayIapReceipt(orderId);
    }

    @Override
    public GooglePlayIapReceipt getOrCreateGooglePlayIapReceipt(GooglePlayIapReceipt googlePlayIapReceipt) {
        return getGooglePlayIapReceiptDao().getOrCreateGooglePlayIapReceipt(googlePlayIapReceipt);
    }

    @Override
    public void deleteGooglePlayIapReceipt(String orderId) {
        getGooglePlayIapReceiptDao().deleteGooglePlayIapReceipt(orderId);
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
            logger.error("Communication with the Google Play services was not successful: {}", e);
            throw new InternalException("Communication with the Google Play services was not successful. Please refer" +
                    " to server logs for more information.");
        }

        final GooglePlayIapReceipt googlePlayIapReceipt =
                getDozerMapper().map(productPurchase, GooglePlayIapReceipt.class);

        googlePlayIapReceipt.setUser(getUser());
        googlePlayIapReceipt.setPurchaseToken(purchaseToken);

        final GooglePlayIapReceipt resultGooglePlayIapReceipt =
                getOrCreateGooglePlayIapReceipt(googlePlayIapReceipt);

        return resultGooglePlayIapReceipt;
    }

    @Override
    public RewardIssuance getOrCreateRewardIssuance(GooglePlayIapReceipt googlePlayIapReceipt) {
        if (PURCHASE_STATE_CANCELED == googlePlayIapReceipt.getPurchaseState()) {
            throw new InvalidDataException("Google Play purchase marked as canceled in purchaseState.");
        }

        GooglePlayApplicationConfiguration googlePlayApplicationConfiguration =
                getGooglePlayApplicationConfiguration();

        final String context = buildGooglePlayIapContextString(googlePlayIapReceipt.getOrderId());

        final Map<String, Object> metadata = generateGooglePlayIapReceiptMetadata();

        try {
            final RewardIssuance resultRewardIssuance = getRewardIssuanceDao().getRewardIssuance(getUser(), context);
            return resultRewardIssuance;
        }
        catch (NotFoundException e) {
            final String productId = googlePlayIapReceipt.getProductId();

            final String itemId = googlePlayApplicationConfiguration.getItemIdForProductId(productId);

            final Integer rewardQuantity = googlePlayApplicationConfiguration.getQuantityForProductId(productId);

            // then, we get a model rep of the given item id
            final Item item = getItemDao().getItemByIdOrName(itemId);

            // we now have everything we need to set up and insert a new reward...
            final Reward reward = new Reward();

            reward.setQuantity(rewardQuantity);
            reward.setItem(item);

            final Reward resultReward = getRewardDao().createReward(reward);

            // once the reward is inserted, we now have everything we need to set up and insert a new issuance...
            final RewardIssuance rewardIssuance = new RewardIssuance();

            rewardIssuance.setReward(resultReward);
            rewardIssuance.setUser(user);
            // we hold onto the reward issuance forever so as not to duplicate an already-redeemed issuance
            rewardIssuance.setType(PERSISTENT);
            rewardIssuance.setContext(context);
            rewardIssuance.setMetadata(metadata);
            rewardIssuance.setSource(GOOGLE_PLAY_IAP_SOURCE);

            final RewardIssuance resultRewardIssuance = getRewardIssuanceDao()
                    .getOrCreateRewardIssuance(rewardIssuance);

            return resultRewardIssuance;
        }
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
                        ANDROID_GOOGLE_PLAY,
                        GooglePlayApplicationConfiguration.class);

        return googlePlayApplicationConfiguration;
    }

    private String getCurrentPlatformApplicationId() {
        GooglePlayApplicationConfiguration googlePlayApplicationConfiguration = getGooglePlayApplicationConfiguration();

        final String platformApplicationId;

        if (googlePlayApplicationConfiguration.getApplicationId() != null) {
            platformApplicationId = googlePlayApplicationConfiguration.getApplicationId();
        } else if (googlePlayApplicationConfiguration.getUniqueIdentifier() != null) {
            platformApplicationId = googlePlayApplicationConfiguration.getUniqueIdentifier();
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

}
