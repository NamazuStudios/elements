package dev.getelements.elements.dao.mongo.application;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.goods.MongoItemDao;
import dev.getelements.elements.dao.mongo.model.application.MongoApplicationConfiguration;
import dev.getelements.elements.dao.mongo.model.application.MongoProductBundle;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.exception.BadQueryException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.item.ItemNotFoundException;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.util.Objects.requireNonNull;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationConfigurationDao implements ApplicationConfigurationDao {

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

        requireNonNull(productBundles, "productBundles");

        for (final var productBundle : productBundles) {
            for (final var productBundleReward : productBundle.getProductBundleRewards()) {
                final String itemNameOrId = productBundleReward.getItemId();
                final MongoItem mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
                if (mongoItem == null) {
                    throw new ItemNotFoundException("Item with name/id: " + itemNameOrId + " not found.");
                }
                productBundleReward.setItemId(mongoItem.getObjectId().toHexString());
            }
        }

        final var mongoProductBundles = productBundles
            .stream()
            .map(pb -> getMapperRegistry().map(pb, MongoProductBundle.class))
            .collect(Collectors.toList());

        final var query = getQueryForApplicationConfiguration(
                configurationClass,
                applicationNameOrId,
                applicationConfigurationNameOrId
        );

        final var resultMongoApplicationConfiguration = new UpdateBuilder()
                .with(set("productBundles", mongoProductBundles))
                .modify(query)
                .execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (resultMongoApplicationConfiguration == null) {
            throw new NotFoundException("Application Configuration with id: " + applicationNameOrId + "not found.");
        }

        return getMapperRegistry().map(resultMongoApplicationConfiguration, configurationClass);

    }

    @Override
    public <T extends ApplicationConfiguration> T createApplicationConfiguration(
            final String applicationNameOrId,
            final T applicationConfiguration) {

        requireNonNull(applicationConfiguration, "applicationNameOrId");
        getValidationHelper().validateModel(applicationConfiguration, Insert.class);

        final var parent = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var mongoTClass = getMongoApplicationConfigurationType(applicationConfiguration.getClass());
        final var mongoApplicationConfiguration = getMapperRegistry().map(applicationConfiguration, mongoTClass);

        mongoApplicationConfiguration.setParent(parent);
        getMongoDBUtils().performV(ds -> getDatastore().insert(mongoApplicationConfiguration));

        return getMapperRegistry().map(mongoApplicationConfiguration, (Class<T>) applicationConfiguration.getClass());

    }

    @Override
    public <T extends ApplicationConfiguration> T updateApplicationConfiguration(
            final String applicationNameOrId,
            final T applicationConfiguration) {

        requireNonNull(applicationConfiguration, "applicationNameOrId");
        getValidationHelper().validateModel(applicationConfiguration, Insert.class);

        final var parent = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var mongoTClass = getMongoApplicationConfigurationType(applicationConfiguration.getClass());
        final var mongoApplicationConfiguration = getMapperRegistry().map(applicationConfiguration, mongoTClass);

        mongoApplicationConfiguration.setParent(parent);
        getMongoDBUtils().performV(ds -> getDatastore().replace(mongoApplicationConfiguration));

        return getMapperRegistry().map(mongoApplicationConfiguration, (Class<T>) applicationConfiguration.getClass());

    }

    @Override
    public void deleteApplicationConfiguration(
            final Class<? extends ApplicationConfiguration> configType,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        final var mongoConfigType = getMongoApplicationConfigurationType(configType);

    }

    @Override
    public <T extends ApplicationConfiguration> Optional<T> findApplicationConfiguration(
            final Class<T> configType,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var mongoConfigType = getMongoApplicationConfigurationType(configType);

        return findMongoApplicationConfiguration(
                mongoConfigType,
                applicationNameOrId,
                applicationConfigurationNameOrId
        ).map(mac -> getMapperRegistry().map(mac, configType));

    }

    public <T extends MongoApplicationConfiguration> Optional<T> findMongoApplicationConfiguration(
            final Class<T> configT,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var query = getQueryForMongoApplicationConfiguration(
                configT,
                applicationNameOrId,
                applicationConfigurationNameOrId
        );

        return Optional.ofNullable(query.first());

    }

    public <T extends ApplicationConfiguration>
    Query<? extends MongoApplicationConfiguration> getQueryForApplicationConfiguration(
            final Class<T> configType,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var mongoConfigType = getMongoApplicationConfigurationType(configType);

        return getQueryForMongoApplicationConfiguration(
                mongoConfigType,
                applicationNameOrId,
                applicationConfigurationNameOrId
        );

    }

    public <T extends MongoApplicationConfiguration>
    Query<T> getQueryForMongoApplicationConfiguration(
            final Class<T> configType,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var parent = getMongoApplicationDao()
                .findActiveMongoApplication(applicationNameOrId);

        if (parent == null) {
            return getDatastore()
                    .find(configType)
                    .filter(eq("_id", null));
        }

        final var nameOrIdFilter = getMongoDBUtils().parse(applicationConfigurationNameOrId)
                .map(objectId -> eq("_id", objectId))
                .orElseGet(() -> eq("name", applicationConfigurationNameOrId));

        return getDatastore()
                .find(configType)
                .filter(nameOrIdFilter, eq("parent", parent));

    }

    public <SourceT extends ApplicationConfiguration>
    Class<? extends MongoApplicationConfiguration> getMongoApplicationConfigurationType(final Class<SourceT> sourceTClass) {

        final var mapper = getMapperRegistry()
                .mappers()
                .filter(m -> m
                        .findSourceType()
                        .map(c -> c.equals(sourceTClass))
                        .orElse(false)
                )
                .filter(m -> m
                        .findDestinationType()
                        .map(MongoApplicationConfiguration.class::isAssignableFrom)
                        .orElse(false)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No mapper found for " + sourceTClass));

        return (Class<? extends MongoApplicationConfiguration>) mapper
                .findDestinationType()
                .orElseThrow(() -> new IllegalArgumentException("No destination type for  " + sourceTClass));

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
