package dev.getelements.elements.dao.mongo.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.goods.MongoItemDao;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.application.MongoApplicationConfiguration;
import dev.getelements.elements.dao.mongo.model.application.MongoProductBundle;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ConfigurationCategory;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationConfigurationDao implements ApplicationConfigurationDao {

    private MapperRegistry beanMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private MongoItemDao mongoItemDao;

    private MapperRegistry dozerMapperRegistry;

    private ObjectMapper objectMapper;

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
                    .map(mac -> getDozerMapper().map(mac, (Class<T>) configurationCategory.getModelClass()))
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

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> getBeanMapper().map(input, ApplicationConfiguration.class), new FindOptions());

    }

    @Override
    public Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(final String applicationNameOrId,
                                                                                   final int offset, final int count,
                                                                                   final String search) {
        return Pagination.empty();
    }

    @Override
    public ApplicationConfiguration updateProductBundles(final String applicationConfigurationId,
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
            .map(pb -> getDozerMapper().map(pb, MongoProductBundle.class))
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

        return getDozerMapper().map(resultMongoApplicationConfiguration, ApplicationConfiguration.class);

    }

    @Override
    public <T extends ApplicationConfiguration> T createApplicationConfiguration(
            final String applicationNameOrId,
            final T applicationConfiguration) {
        return null;
    }

    @Override
    public <T extends ApplicationConfiguration> T updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationId,
            final T applicationConfiguration) {
        return null;
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

    public MapperRegistry getBeanMapper() {
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
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

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
