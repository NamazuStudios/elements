package dev.getelements.elements.dao.mongo.application;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.goods.MongoItemDao;
import dev.getelements.elements.dao.mongo.model.application.*;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ConfigurationCategory;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.exception.BadQueryException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.application.ApplicationNotFoundException;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.sdk.model.application.ConfigurationCategory.*;
import static dev.morphia.query.filters.Filters.*;
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

    private MapperRegistry mapperRegistry;

    private BooleanQueryParser booleanQueryParser;

    private MongoItemDao mongoItemDao;

    @Override
    public <T extends ApplicationConfiguration> List<T> getAllActiveApplicationConfigurations(
            final String applicationNameOrId,
            final Class<T> configurationClass) {

        final var parent = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final var query = getDatastore().find(MongoApplicationConfiguration.class);

        query.filter(
                exists("name"),
                eq("parent", parent),
                eq("type", configurationClass.getName())
        );

        final List<T> applicationConfigurations;
        final var mapper = getMapperRegistry().getMapper(MongoApplicationConfiguration.class, configurationClass);

        try (var iterator = query.iterator()) {
            applicationConfigurations = iterator
                    .toList()
                    .stream()
                    .map(mapper::forward)
                    .collect(Collectors.toList());
        }

        return applicationConfigurations;

    }

    @Override
    public Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(
            final String applicationNameOrId,
            final int offset, final int count) {

        final var parent = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoApplicationConfiguration> query = getDatastore().find(MongoApplicationConfiguration.class);

        query.filter(Filters.and(
            exists("name"),
            eq("parent", parent)
        ));

        return paginationFromQuery(query, offset, count);

    }

    @Override
    public Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(
            final String applicationNameOrId,
            final int offset, final int count,
            final String search) {

        final String trimmedSearch = nullToEmpty(search).trim();

        if (trimmedSearch.isEmpty()) {
            throw new InvalidDataException("search must be specified.");
        }

        final var query = getBooleanQueryParser()
                .parse(MongoApplicationConfiguration.class, search)
                .orElseGet(() -> parseTextQuery(search));

        return getMongoDBUtils().isIndexedQuery(query)
                ? paginationFromQuery(query, offset, count)
                : Pagination.empty();

    }

    private Query<MongoApplicationConfiguration> parseTextQuery(final String search) {

        final String trimmedSearch = nullToEmpty(search).trim();

        if (trimmedSearch.isEmpty()) {
            throw new BadQueryException("search must be specified.");
        }

        final Query<MongoApplicationConfiguration> profileQuery = getDatastore()
                .find(MongoApplicationConfiguration.class);

        return profileQuery.filter(
                exists("name"),
                text(trimmedSearch)
        );

    }

    private Pagination<ApplicationConfiguration> paginationFromQuery(
            final Query<MongoApplicationConfiguration> query,
            final int offset, final int count) {
        return getMongoDBUtils().paginationFromQuery(
                query,
                offset, count,
                input -> getMapperRegistry().map(
                        input,
                        ApplicationConfiguration.class),
                new FindOptions()
        );
    }

    @Override
    public <T extends ApplicationConfiguration>
    T updateProductBundles(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final Class<T> configurationClass,
            final List<ProductBundle> productBundles) {

        final var mapper = getMapperRegistry()
                .mappers()
                .filter(m -> m.findSourceType().isPresent() && m.findSourceType().get().equals(configurationClass))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No mapper found for " + configurationClass));

        final var mongApplicationConfigurationType = mapper
                .findDestinationType()
                .filter(MongoApplicationConfiguration.class::isAssignableFrom)
                .orElseThrow(() -> new IllegalArgumentException("No destination type for  " + configurationClass));

        final var mongoApplicationConfiguration = findMongoApplicationConfiguration(
                mongApplicationConfigurationType,
                applicationNameOrId,
                applicationConfigurationNameOrId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found:" + applicationNameOrId));

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
                            applicationNameOrId +
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

    public <T extends MongoMatchmakingApplicationConfiguration> Optional<T> findMongoApplicationConfiguration(
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

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

}
