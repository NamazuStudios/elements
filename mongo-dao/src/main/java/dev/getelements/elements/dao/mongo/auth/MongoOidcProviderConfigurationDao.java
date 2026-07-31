package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.auth.MongoOidcProviderConfiguration;
import dev.getelements.elements.sdk.dao.OidcProviderConfigurationDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.exception.auth.OidcProviderConfigurationNotFoundException;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoOidcProviderConfigurationDao implements OidcProviderConfigurationDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<OidcProviderConfiguration> getProviderConfigurations(final int offset,
                                                                            final int count,
                                                                            final List<String> tags) {
        final var query = getDatastore().find(MongoOidcProviderConfiguration.class);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, this::transform, new FindOptions());
    }

    @Override
    public Optional<OidcProviderConfiguration> findProviderConfiguration(final String providerConfigurationId) {
        return getMongoDBUtils()
                .parse(providerConfigurationId)
                .map(objectId -> getDatastore()
                        .find(MongoOidcProviderConfiguration.class)
                        .filter(eq("_id", objectId))
                        .first())
                .map(this::transform);
    }

    @Override
    public Optional<OidcProviderConfiguration> findByProvider(final String provider) {
        final var entity = getDatastore().find(MongoOidcProviderConfiguration.class)
                .filter(eq("provider", provider))
                .first();
        return Optional.ofNullable(entity).map(this::transform);
    }

    @Override
    public OidcProviderConfiguration createProviderConfiguration(final OidcProviderConfiguration providerConfiguration) {
        getValidationHelper().validateModel(providerConfiguration, ValidationGroups.Insert.class);
        final var entity = getBeanMapper().map(providerConfiguration, MongoOidcProviderConfiguration.class);
        final var result = getMongoDBUtils().perform(ds -> getDatastore().save(entity));
        return transform(result);
    }

    @Override
    public OidcProviderConfiguration updateProviderConfiguration(final OidcProviderConfiguration providerConfiguration) {

        getValidationHelper().validateModel(providerConfiguration, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(
                providerConfiguration.getId(),
                OidcProviderConfigurationNotFoundException::new
        );

        final var query = getDatastore().find(MongoOidcProviderConfiguration.class);
        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder();
        builder.with(set("provider", providerConfiguration.getProvider()));
        builder.with(set("discoveryUrl", providerConfiguration.getDiscoveryUrl()));
        builder.with(set("clientId", providerConfiguration.getClientId()));
        builder.with(set("clientSecret", providerConfiguration.getClientSecret()));
        builder.with(set("scopes", providerConfiguration.getScopes()));
        builder.with(set("redirectUri", providerConfiguration.getRedirectUri()));
        builder.with(set("extraAuthorizeParams", providerConfiguration.getExtraAuthorizeParams()));
        builder.with(set("tokenEndpointAuthMethod", providerConfiguration.getTokenEndpointAuthMethod()));

        final var entity = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (entity == null) {
            throw new OidcProviderConfigurationNotFoundException("Provider configuration not found: " + providerConfiguration.getId());
        }

        return transform(entity);

    }

    @Override
    public void deleteProviderConfiguration(final String providerConfigurationId) {

        final var objectId = getMongoDBUtils().parseOrThrow(
                providerConfigurationId,
                OidcProviderConfigurationNotFoundException::new
        );

        final var result = getDatastore().find(MongoOidcProviderConfiguration.class)
                .filter(eq("_id", objectId))
                .delete();

        if (result.getDeletedCount() == 0) {
            throw new OidcProviderConfigurationNotFoundException("Provider configuration not found: " + providerConfigurationId);
        }

    }

    private OidcProviderConfiguration transform(final MongoOidcProviderConfiguration entity) {
        return getBeanMapper().map(entity, OidcProviderConfiguration.class);
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

    public MapperRegistry getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapper) {
        this.beanMapper = beanMapper;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
