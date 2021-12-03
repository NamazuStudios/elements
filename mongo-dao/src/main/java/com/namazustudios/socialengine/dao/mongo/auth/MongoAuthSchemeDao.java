package com.namazustudios.socialengine.dao.mongo.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.AuthSchemeDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.auth.MongoAuthScheme;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoWallet;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.auth.*;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;

public class MongoAuthSchemeDao implements AuthSchemeDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ObjectMapper objectMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<AuthScheme> getAuthSchemes(int offset, int count, List<String> tags) {

        final Query<MongoAuthScheme> mongoQuery = getDatastore().find(MongoAuthScheme.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.filter(Filters.in("tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public AuthScheme getAuthScheme(String authSchemeId) {
        var mongoAuthScheme = getDatastore().find(MongoAuthScheme.class)
                .filter(Filters.or(
                                Filters.eq("_id", authSchemeId)
                        )
                ).first();

        if(null == mongoAuthScheme) {
            throw new NotFoundException("Unable to find auth scheme with an id of " + authSchemeId);
        }

        return transform(mongoAuthScheme);
    }

    @Override
    public UpdateAuthSchemeResponse updateAuthScheme(UpdateAuthSchemeRequest updateAuthSchemeRequest) {
        getValidationHelper().validateModel(updateAuthSchemeRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(updateAuthSchemeRequest.getAuthSchemeId());
        final var query = getDatastore().find(MongoAuthScheme.class);

        final var builder = new UpdateBuilder();

        query.filter(eq("_id", objectId));

        final MongoAuthScheme mongoAuthScheme = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoAuthScheme == null) {
            throw new NotFoundException("auth scheme not found: " + updateAuthSchemeRequest.getAuthSchemeId());
        }

        getObjectIndex().index(mongoAuthScheme);

        var authScheme = transform(mongoAuthScheme);
        var response = new UpdateAuthSchemeResponse();

        // serialize auth scheme
        try {
            response.scheme = objectMapper.writeValueAsString(authScheme);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e);
        }

        if (updateAuthSchemeRequest.getRegenerate()) {
            if (updateAuthSchemeRequest.getPubKey() != null)
            {
                throw new BadRequestException();
            }
            // TODO regenerate pub/private key pair
        } else {
            response.publicKey = updateAuthSchemeRequest.getPubKey();
        }

        return response;
    }

    @Override
    public CreateAuthSchemeResponse createAuthScheme(CreateAuthSchemeRequest authSchemeRequest) {

        if (authSchemeRequest == null) {
            throw new InvalidDataException("Auth Scheme request must not be null.");
        }

        var response = new CreateAuthSchemeResponse();
        var mongoAuthScheme = getBeanMapper().map(authSchemeRequest, MongoAuthScheme.class);

        try {
            getDatastore().save(mongoAuthScheme);
            getObjectIndex().index(mongoAuthScheme);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        var authScheme = transform(mongoAuthScheme);
        getValidationHelper().validateModel(authScheme);

        // serialize auth scheme
        try {
            response.scheme = objectMapper.writeValueAsString(authScheme);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(e);
        }

        // generate public/private key
        if (authSchemeRequest.getPubKey() == null) {
            //TODO generate pub/private key pair
        } else {
            response.publicKey = authSchemeRequest.getPubKey();
        }

        return response;
    }

    @Override
    public void deleteAuthScheme(String authSchemeId) {
        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(authSchemeId);
        final var query = getDatastore().find(MongoNeoWallet.class);

        query.filter(eq("_id", objectId));
        query.delete();
    }

    private AuthScheme transform(MongoAuthScheme mongoAuthScheme)
    {
        return getBeanMapper().map(mongoAuthScheme, AuthScheme.class);
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }
}
