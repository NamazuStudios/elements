package dev.getelements.elements.dao.mongo.application;

import dev.getelements.elements.dao.mongo.model.application.*;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.goods.MongoItemDao;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ConfigurationCategory;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;

import javax.xml.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.sdk.model.application.ConfigurationCategory.*;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationConfigurationDao implements ApplicationConfigurationDao {

    public static final Map<ConfigurationCategory, Class<? extends MongoApplicationConfiguration>> TYPE_MAPPING;

    static {
        final Map<ConfigurationCategory, Class<? extends MongoApplicationConfiguration>> map = new EnumMap<>(ConfigurationCategory.class);
        map.put(MATCHMAKING, MongoMatchmakingApplicationConfiguration.class);
        map.put(PSN_PS4, MongoPSNApplicationConfiguration.class);
        map.put(PSN_PS5, MongoPSNApplicationConfiguration.class);
        map.put(IOS_APP_STORE, MongoPSNApplicationConfiguration.class);
        map.put(ANDROID_GOOGLE_PLAY, MongoGooglePlayApplicationConfiguration.class);
        map.put(FACEBOOK, MongoFacebookApplicationConfiguration.class);
        map.put(FIREBASE, MongoFirebaseApplicationConfiguration.class);
        TYPE_MAPPING = Collections.unmodifiableMap(map);
    }

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private MongoItemDao mongoItemDao;

    private MapperRegistry mapperRegistry;

    @Override
    public <T extends ApplicationConfiguration> List<T> getApplicationConfigurationsForApplication(
            final String applicationNameOrId,
            final ConfigurationCategory configurationCategory) {
        final MongoApplication parent = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoApplicationConfiguration> query = getDatastore().find(MongoApplicationConfiguration.class);

        query.filter(eq("parent", parent));
        query.filter(eq("category", configurationCategory));

        final List<T> applicationConfigurations;

        try (var iterator = query.iterator()) {
            applicationConfigurations = iterator
                    .toList()
                    .stream()
                    .map(mac -> getMapperRegistry().map(mac, (Class<T>) configurationCategory.getModelClass()))
                    .collect(Collectors.toList());
        }

        return applicationConfigurations;

    }

    @Override
    public Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(final String applicationNameOrId,
                                                                                   final int offset, final int count) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoApplicationConfiguration> query = getDatastore().find(MongoApplicationConfiguration.class);

        query.filter(Filters.and(
           eq("active", true),
           eq("parent", mongoApplication)
        ));

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> getMapperRegistry().map(input, ApplicationConfiguration.class), new FindOptions());

    }

    @Override
    public Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(final String applicationNameOrId,
                                                                                   final int offset, final int count,
                                                                                   final String search) {
        return Pagination.empty();
    }

    @Override
    public ApplicationConfiguration updateProductBundles(
            final String applicationConfigurationId,
            final List<ProductBundle> productBundles) {

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(applicationConfigurationId);
        final var query = getDatastore().find(MongoApplicationConfiguration.class);
        query.filter(eq("_id", objectId));

        // make sure to convert any item name strings to item id strings
        for (final var productBundle : productBundles) {
            for (final var productBundleReward : productBundle.getProductBundleRewards()) {
                final String itemNameOrId = productBundleReward.getItemId();
                final MongoItem mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
                if (mongoItem == null) {
                    throw new NotFoundException("Item with name/id: " + itemNameOrId + " not found.");
                }
                productBundleReward.setItemId(mongoItem.getObjectId().toHexString());
            }
        }

        final var mongoProductBundles = productBundles
            .stream()
            .map(pb -> getMapperRegistry().map(pb, MongoProductBundle.class))
            .collect(Collectors.toList());

        final var resultMongoApplicationConfiguration = new UpdateBuilder()
                .with(set("productBundles", mongoProductBundles))
                .modify(query)
                .execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (resultMongoApplicationConfiguration == null) {
            throw new NotFoundException(
                    "Application Configuration with id: " +
                    applicationConfigurationId +
                    "not found."
            );
        }

        return getMapperRegistry().map(resultMongoApplicationConfiguration, ApplicationConfiguration.class);

    }

    @Override
    public <T extends ApplicationConfiguration> T createApplicationConfiguration(
            final String applicationNameOrId,
            final T applicationConfiguration) {

        getValidationHelper().validateModel(applicationConfiguration, Insert.class);

        final var category = applicationConfiguration.getCategory();

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        } else if (category.getModelClass() != applicationConfiguration.getClass()) {
            throw new IllegalArgumentException("Category class must be of type " + applicationConfiguration
                    .getCategory()
                    .getModelClass());
        }

        final var mongoTClass = TYPE_MAPPING.get(applicationConfiguration.getCategory());
        final var mongApplicationConfiguration = getMapperRegistry().map(applicationConfiguration, mongoTClass);
        getMongoDBUtils().performV(ds -> getDatastore().insert(mongApplicationConfiguration));

        return getMapperRegistry().map(mongApplicationConfiguration, (Class<T>) category.getModelClass());

    }

    @Override
    public <T extends ApplicationConfiguration> T updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationId,
            final T applicationConfiguration) {

        getValidationHelper().validateModel(applicationConfiguration, Update.class);

        final var category = applicationConfiguration.getCategory();

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        } else if (category.getModelClass() != applicationConfiguration.getClass()) {
            throw new IllegalArgumentException("Category class must be of type " + applicationConfiguration
                    .getCategory()
                    .getModelClass());
        }

        final var mongoTClass = TYPE_MAPPING.get(applicationConfiguration.getCategory());
        final var mongApplicationConfiguration = getMapperRegistry().map(applicationConfiguration, mongoTClass);
        getMongoDBUtils().performV(ds -> getDatastore().save(mongApplicationConfiguration));

        return getMapperRegistry().map(mongApplicationConfiguration, (Class<T>) category.getModelClass());

    }

    @Override
    public void deleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationId) {

    }

    @Override
    public <T extends ApplicationConfiguration> Optional<T> findApplicationConfiguration(
            final Class<T> configT,
            final String applicationNameOrId,
            final String applicationConfigurationId) {
        return Optional.empty();
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public MongoItemDao getMongoItemDao() {
        return mongoItemDao;
    }

    @Inject
    public void setMongoItemDao(MongoItemDao mongoItemDao) {
        this.mongoItemDao = mongoItemDao;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }
}
