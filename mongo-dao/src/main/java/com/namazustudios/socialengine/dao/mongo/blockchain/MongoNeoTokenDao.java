package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.mongodb.DuplicateKeyException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoWallet;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoToken;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.CreateNeoTokenRequest;
import com.namazustudios.socialengine.model.blockchain.NeoToken;
import com.namazustudios.socialengine.model.blockchain.UpdateNeoTokenRequest;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;

import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoNeoTokenDao implements NeoTokenDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    private MongoUserDao mongoUserDao;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public Pagination<NeoToken> getTokens(int offset, int count, List<String> tags, String search) {

        final String trimmedSearch = nullToEmpty(search).trim();
        final Query<MongoNeoToken> mongoQuery = getDatastore().find(MongoNeoToken.class);

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
    public NeoToken getToken(String tokenIdOrName) {

        var mongoToken = getDatastore().find(MongoNeoToken.class)
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
    public NeoToken updateToken(UpdateNeoTokenRequest updateNeoTokenRequest) {
        getValidationHelper().validateModel(updateNeoTokenRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(updateNeoTokenRequest.getTokenId());
        final var query = getDatastore().find(MongoNeoToken.class);

        final var builder = new UpdateBuilder();

        query.filter(eq("_id", objectId));

        builder.with(
                set("name", nullToEmpty(updateNeoTokenRequest.getToken().getName()).trim())
        );

        final MongoNeoToken mongoNeoToken = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoNeoToken == null) {
            throw new NotFoundException("NeoToken not found: " + updateNeoTokenRequest.getTokenId());
        }

        getObjectIndex().index(mongoNeoToken);
        return transform(mongoNeoToken);
    }

    @Override
    public NeoToken createToken(CreateNeoTokenRequest tokenRequest) {

        getValidationHelper().validateModel(tokenRequest, ValidationGroups.Insert.class);

        var mongoToken = getBeanMapper().map(tokenRequest, MongoNeoToken.class);

        try {
            getDatastore().save(mongoToken);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }
        getObjectIndex().index(mongoToken);

        final Query<MongoNeoToken> query = getDatastore().find(MongoNeoToken.class);

        return transform(mongoToken);
    }

    @Override
    public void deleteToken(String templateId) {
        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(templateId);
        final var query = getDatastore().find(MongoNeoWallet.class);

        query.filter(eq("_id", objectId));
        query.delete();
    }


    private NeoToken transform(MongoNeoToken token)
    {
        return getBeanMapper().map(token, NeoToken.class);
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
