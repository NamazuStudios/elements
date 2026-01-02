package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.sdk.dao.OAuth2AuthSchemeDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.auth.MongoOAuth2AuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
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
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoOAuth2AuthSchemeDao implements OAuth2AuthSchemeDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapperRegistry;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<OAuth2AuthScheme> getAuthSchemes(final int offset,
                                                     final int count,
                                                     final List<String> tags) {

        final var mongoQuery = getDatastore().find(MongoOAuth2AuthScheme.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.filter(in("tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform, new FindOptions());

    }

    @Override
    public Optional<OAuth2AuthScheme> findAuthScheme(final String authSchemeNameOrId) {

        final var query = getMongoDBUtils()
                .parse(authSchemeNameOrId)
                .map(objectId -> getDatastore().find(MongoOAuth2AuthScheme.class).filter(eq("_id", objectId)))
                .orElseGet(() -> getDatastore().find(MongoOAuth2AuthScheme.class).filter(eq("name", authSchemeNameOrId)));

        return Optional.ofNullable(query.first()).map(this::transform);
    }

    @Override
    public OAuth2AuthScheme createAuthScheme(final OAuth2AuthScheme authScheme) {
        getValidationHelper().validateModel(authScheme, ValidationGroups.Insert.class);
        final var mongoOAuth2AuthScheme = getBeanMapper().map(authScheme, MongoOAuth2AuthScheme.class);
        final var result = getMongoDBUtils().perform(ds -> getDatastore().save(mongoOAuth2AuthScheme));
        return transform(result);
    }

    @Override
    public OAuth2AuthScheme updateAuthScheme(final OAuth2AuthScheme authScheme) {

        getValidationHelper().validateModel(authScheme, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(authScheme.getId(), AuthSchemeNotFoundException::new);
        final var query = getDatastore().find(MongoOAuth2AuthScheme.class);
        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder();
        builder.with(set("name", authScheme.getName()));
        builder.with(set("validationUrl", authScheme.getValidationUrl()));
        builder.with(set("params", authScheme.getParams()));
        builder.with(set("headers", authScheme.getHeaders()));
        builder.with(set("body", authScheme.getBody()));
        builder.with(set("method", authScheme.getMethod()));
        builder.with(set("bodyType", authScheme.getBodyType()));
        builder.with(set("responseIdMapping", authScheme.getResponseIdMapping()));
        builder.with(set("responseValidMapping", authScheme.getResponseValidMapping()));
        builder.with(set("responseValidExpectedValue", authScheme.getResponseValidExpectedValue()));
        builder.with(set("validStatusCodes", authScheme.getValidStatusCodes()));

        final MongoOAuth2AuthScheme mongoOAuth2AuthScheme = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoOAuth2AuthScheme == null) {
            throw new AuthSchemeNotFoundException("Auth scheme not found: " + authScheme.getId());
        }

        return transform(mongoOAuth2AuthScheme);

    }

    @Override
    public void deleteAuthScheme(final String authSchemeId) {

        final var objectId = getMongoDBUtils().parseOrThrow(authSchemeId, AuthSchemeNotFoundException::new);

        final var query = getDatastore().find(MongoOAuth2AuthScheme.class);
        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder();
        builder.with(unset("name"));
        builder.with(unset("validationUrl"));
        builder.with(unset("params"));
        builder.with(unset("headers"));
        builder.with(unset("body"));
        builder.with(unset("method"));
        builder.with(unset("bodyType"));
        builder.with(unset("responseIdMapping"));
        builder.with(unset("responseValidMapping"));
        builder.with(unset("responseValidExpectedValue"));
        builder.with(unset("validStatusCodes"));

        final MongoOAuth2AuthScheme mongoOidcAuthScheme = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoOidcAuthScheme == null) {
            throw new AuthSchemeNotFoundException("Auth scheme not found: " + authSchemeId);
        }

    }

    private OAuth2AuthScheme transform(MongoOAuth2AuthScheme mongoOAuth2AuthScheme) {
        return getBeanMapper().map(mongoOAuth2AuthScheme, OAuth2AuthScheme.class);
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
