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
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.exception.BadQueryException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.application.ApplicationConfigurationNotFoundException;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import jakarta.inject.Inject;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.util.Objects.requireNonNull;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationConfigurationDao implements ApplicationConfigurationDao {

    public static final String PRODUCT_BUNDLES_PROPERTY = "productBundles";

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

        final var parent = getMongoApplicationDao().getMongoApplication(applicationNameOrId);

        final var query = getDatastore().find(MongoApplicationConfiguration.class);

        query.filter(
                exists("name"),
                eq("parent", parent),
                eq("type", configurationClass.getName())
        );

        final List<T> applicationConfigurations;

        // Cast. Damnit. Cast.
        final var mongoApplicationConfigurationClass = (Class<MongoApplicationConfiguration>) getMongoApplicationConfigurationType(configurationClass);
        final var mapper = getMapperRegistry().getMapper(mongoApplicationConfigurationClass, configurationClass);

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
        final var parent = getMongoApplicationDao().getMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(MongoApplicationConfiguration.class);
        query.filter(exists("name"), eq("parent", parent));
        return paginationFromQuery(query, offset, count);
    }

    @Override
    public Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(
            final String applicationNameOrId,
            final int offset, final int count,
            final String search) {

        final var trimmed = nullToEmpty(search).trim();
        final var parent = getMongoApplicationDao().getMongoApplication(applicationNameOrId);

        final var query = getBooleanQueryParser()
                .parse(MongoApplicationConfiguration.class, search)
                .orElseGet(() -> parseTextQuery(trimmed));

        query.filter(exists("name"), eq("parent", parent));

        return getMongoDBUtils().isIndexedQuery(query)
                ? paginationFromQuery(query, offset, count)
                : Pagination.empty();

    }

    private Query<MongoApplicationConfiguration> parseTextQuery(final String search) {

        final String trimmedSearch = nullToEmpty(search).trim();

        if (trimmedSearch.isEmpty()) {
            throw new BadQueryException("search must be specified.");
        }

        final Query<MongoApplicationConfiguration> query = getDatastore()
                .find(MongoApplicationConfiguration.class);

        query.filter(exists("name"));

        if (search != null && !search.isBlank()) {
            query.filter(text(trimmedSearch));
        }

        return query;

    }

    private Pagination<ApplicationConfiguration> paginationFromQuery(
            final Query<MongoApplicationConfiguration> query,
            final int offset, final int count) {

        final var mapper = getMapperRegistry()
                .mappers()
                .filter(m -> m
                        .findSourceType()
                        .map(c -> c.equals(MongoApplicationConfiguration.class)).orElse(false)
                )
                .filter(m -> m
                        .findDestinationType()
                        .map(c -> c.equals(ApplicationConfiguration.class)).orElse(false)
                )
                .findFirst()
                .map(m -> (MapperRegistry.Mapper<MongoApplicationConfiguration, ApplicationConfiguration>)m)
                .orElseThrow(InternalException::new);

        return getMongoDBUtils().paginationFromQuery(
                query,
                offset, count,
                o -> mapper.forward(o),
                new FindOptions()
        );

    }

    @Override
    public <T extends ApplicationConfiguration> T createApplicationConfiguration(
            final String applicationNameOrId,
            final T applicationConfiguration) {

        requireNonNull(applicationConfiguration, "applicationNameOrId");
        getValidationHelper().validateModel(applicationConfiguration, Insert.class);
        normalizeProductBundles(applicationConfiguration);

        final var parent = getMongoApplicationDao().getMongoApplication(applicationNameOrId);
        final var mongoTClass = getMongoApplicationConfigurationType(applicationConfiguration.getClass());
        final var mongoApplicationConfiguration = getMapperRegistry().map(applicationConfiguration, mongoTClass);

        mongoApplicationConfiguration.setParent(parent);
        mongoApplicationConfiguration.setType(applicationConfiguration.getClass().getName());

        getMongoDBUtils().performV(ds -> getDatastore().insert(mongoApplicationConfiguration));

        return getMapperRegistry().map(mongoApplicationConfiguration, (Class<T>) applicationConfiguration.getClass());

    }

    @Override
    public <T extends ApplicationConfiguration> T updateApplicationConfiguration(
            final String applicationNameOrId,
            final T applicationConfiguration) {

        requireNonNull(applicationConfiguration, "applicationNameOrId");
        getValidationHelper().validateModel(applicationConfiguration, Update.class);
        normalizeProductBundles(applicationConfiguration);

        final var parent = getMongoApplicationDao().getMongoApplication(applicationNameOrId);
        final var mongoTClass = getMongoApplicationConfigurationType(applicationConfiguration.getClass());
        final var mongoApplicationConfiguration = getMapperRegistry().map(applicationConfiguration, mongoTClass);

        mongoApplicationConfiguration.setParent(parent);
        mongoApplicationConfiguration.setType(applicationConfiguration.getClass().getName());

        final var result = getMongoDBUtils().perform(ds -> getDatastore().replace(mongoApplicationConfiguration));
        return getMapperRegistry().map(result, (Class<T>) applicationConfiguration.getClass());

    }

    public void normalizeProductBundles(final ApplicationConfiguration applicationConfiguration) {

        // This is kind of a hack that needs to addressed later. Those with product bundles are an edge case
        // and should be better handled in the future.

        try {

            final var nfo = Introspector.getBeanInfo(applicationConfiguration.getClass());

            final var descriptors = Stream.of(nfo.getPropertyDescriptors())
                    .filter(pd -> pd.getName().equals(PRODUCT_BUNDLES_PROPERTY))
                    .filter(pd -> List.class.isAssignableFrom(pd.getPropertyType()))
                    .collect(Collectors.toList());

            for (final var pd : descriptors) {
                final var getter = (List<ProductBundle>) pd.getReadMethod().invoke(applicationConfiguration);
                final var checked = Collections.checkedList(getter, ProductBundle.class);
                final var normalized = getNormalizedProductBundles(checked);
                pd.getWriteMethod().invoke(applicationConfiguration, normalized);
            }

        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ex) {
            throw new InternalException(ex);
        }

    }

    @Override
    public <T extends ApplicationConfiguration>
    T updateProductBundles(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final Class<T> configurationClass,
            final List<ProductBundle> productBundles) {

        requireNonNull(productBundles, "productBundles");

        final var mongoProductBundles = getNormalizedProductBundles(productBundles)
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

    public List<ProductBundle> getNormalizedProductBundles(final List<ProductBundle> productBundles) {
        return productBundles == null
                ? null
                : productBundles
                    .stream()
                    .map(getValidationHelper()::validateModel)
                    .peek(pb -> pb.getProductBundleRewards()
                        .forEach(reward -> {
                            final String itemNameOrId = reward.getItemId();
                            final MongoItem mongoItem = getMongoItemDao().getMongoItemByNameOrId(itemNameOrId);
                            reward.setItemId(mongoItem.getObjectId().toHexString());
                        }))
                    .toList();
    }

    @Override
    public void deleteApplicationConfiguration(
            final Class<? extends ApplicationConfiguration> configType,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final var query = getQueryForApplicationConfiguration(
                configType,
                applicationNameOrId,
                applicationConfigurationNameOrId);

        final var result = query.delete();

        if (result.getDeletedCount() == 0) {
            throw new ApplicationConfigurationNotFoundException();
        }

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

        final var parent = getMongoApplicationDao().findMongoApplication(applicationNameOrId);

        if (parent == null) {
            return getDatastore()
                    .find(configType)
                    .filter(eq("_id", null));
        }

        final var nameOrIdFilter = getMongoDBUtils().parse(applicationConfigurationNameOrId)
                .map(oid -> eq("_id", oid))
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
