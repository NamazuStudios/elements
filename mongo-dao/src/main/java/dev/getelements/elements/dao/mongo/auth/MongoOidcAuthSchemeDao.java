package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.sdk.dao.OidcAuthSchemeDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.auth.MongoOidcAuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoOidcAuthSchemeDao implements OidcAuthSchemeDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapperRegistry;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<OidcAuthScheme> getAuthSchemes(final int offset,
                                                     final int count,
                                                     final List<String> tags) {

        final var mongoQuery = getDatastore().find(MongoOidcAuthScheme.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.filter(in("tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform, new FindOptions());

    }

    @Override
    public Optional<OidcAuthScheme> findAuthScheme(final String authSchemeIssuerNameOrId) {

        final var query = getMongoDBUtils()
                .parse(authSchemeIssuerNameOrId)
                .map(objectId -> getDatastore()
                        .find(MongoOidcAuthScheme.class)
                        .filter(exists("issuer"))
                        .filter(eq("_id", objectId))
                ).orElseGet(() -> getDatastore()
                        .find(MongoOidcAuthScheme.class)
                        .filter(
                            or(
                                eq("name", authSchemeIssuerNameOrId),
                                eq("issuer", authSchemeIssuerNameOrId)
                            )
                        )
                );


        return Optional.ofNullable(query.first()).map(this::transform);

    }

    @Override
    public OidcAuthScheme createAuthScheme(final OidcAuthScheme authScheme) {
        getValidationHelper().validateModel(authScheme, ValidationGroups.Insert.class);
        final var mongoOidcAuthScheme = getBeanMapper().map(authScheme, MongoOidcAuthScheme.class);
        final var result = getMongoDBUtils().perform(ds -> getDatastore().save(mongoOidcAuthScheme));
        return transform(result);
    }

    @Override
    public OidcAuthScheme updateAuthScheme(final OidcAuthScheme authScheme) {

        getValidationHelper().validateModel(authScheme, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(
                authScheme.getId(),
                AuthSchemeNotFoundException::new
        );

        final var query = getDatastore().find(MongoOidcAuthScheme.class);
        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder();
        builder.with(set("name", authScheme.getName()));
        builder.with(set("keys", authScheme.getKeys()));
        builder.with(set("issuer", authScheme.getIssuer()));
        builder.with(set("keysUrl", authScheme.getKeysUrl()));
        builder.with(set("mediaType", authScheme.getMediaType()));

        final MongoOidcAuthScheme mongoOidcAuthScheme = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoOidcAuthScheme == null) {
            throw new AuthSchemeNotFoundException("Auth scheme not found: " + authScheme.getId());
        }

        return transform(mongoOidcAuthScheme);

    }

    @Override
    public void deleteAuthScheme(final String authSchemeId) {

        final var objectId = getMongoDBUtils().parseOrThrow(authSchemeId, AuthSchemeNotFoundException::new);

        final var result = getDatastore()
                .find(MongoOidcAuthScheme.class)
                .filter(eq("_id", objectId))
                .delete();

        if (result.getDeletedCount() == 0) {
            throw new AuthSchemeNotFoundException("Auth scheme not found: " + authSchemeId);
        }

    }

    private OidcAuthScheme transform(MongoOidcAuthScheme mongoOidcAuthScheme) {
        return getBeanMapper().map(mongoOidcAuthScheme, OidcAuthScheme.class);
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getBeanMapper() {
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
