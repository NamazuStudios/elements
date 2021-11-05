package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.mongodb.DuplicateKeyException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.TokenDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoWallet;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoToken;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.CreateTokenRequest;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.UpdateTokenRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.dozer.Mapper;

import javax.inject.Inject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoTokenDao implements TokenDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    private MongoUserDao mongoUserDao;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public Pagination<Token> getTokens(int offset, int count, List<String> tags, String search) {

        final String trimmedSearch = nullToEmpty(search).trim();
        final Query<MongoToken> mongoQuery = getDatastore().find(MongoToken.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.filter(Filters.in("tags", tags));
        }

        if (!trimmedSearch.isEmpty()) {
            mongoQuery.filter(
                    Filters.or(
                            Filters.regex("name").pattern(Pattern.compile(trimmedSearch)),
                            Filters.regex("type").pattern(Pattern.compile(trimmedSearch))
                    )
            );
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, input -> transform(input), new FindOptions());
    }

    @Override
    public Token getToken(String tokenIdOrName) {

        var mongoToken = getDatastore().find(MongoToken.class)
                .filter(Filters.or(
                            Filters.eq("_id", tokenIdOrName),
                            Filters.eq("name", tokenIdOrName)
                        )
                ).first();

        if(null == mongoToken) {
            throw new NotFoundException("Unable to find item with an id of " + tokenIdOrName);
        }

        return transform(mongoToken);
    }

    @Override
    public Token updateToken(UpdateTokenRequest updateTokenRequest) {
        getValidationHelper().validateModel(updateTokenRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(updateTokenRequest.getTokenId());
        final var query = getDatastore().find(MongoToken.class);

        final var builder = new UpdateBuilder();

        query.filter(eq("_id", objectId));

        builder.with(
                set("name", nullToEmpty(updateTokenRequest.getName()).trim())
        );

        final MongoToken mongoToken = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoToken == null) {
            throw new NotFoundException("Token not found: " + updateTokenRequest.getTokenId());
        }

        getObjectIndex().index(mongoToken);
        return transform(mongoToken);
    }

    @Override
    public Token createToken(CreateTokenRequest tokenRequest) {

        getValidationHelper().validateModel(tokenRequest, ValidationGroups.Insert.class);

        var mongoToken = getBeanMapper().map(tokenRequest, MongoToken.class);

        try {
            getDatastore().save(mongoToken);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }
        getObjectIndex().index(mongoToken);

        final Query<MongoToken> query = getDatastore().find(MongoToken.class);
        query.filter(eq("_id", mongoToken.getId()));

        return transform(mongoToken);
    }

    @Override
    public void deleteToken(String templateId) {
        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(templateId);
        final var query = getDatastore().find(MongoNeoWallet.class);

        query.filter(eq("_id", objectId));
        query.delete();
    }


    private Token transform(MongoToken token)
    {
        return getBeanMapper().map(token, Token.class);
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

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }
}
