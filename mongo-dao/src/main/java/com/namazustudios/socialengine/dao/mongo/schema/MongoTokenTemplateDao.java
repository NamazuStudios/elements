package com.namazustudios.socialengine.dao.mongo.schema;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.TokenTemplateDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.blockchain.MongoNeoSmartContractDao;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoSmartContract;
import com.namazustudios.socialengine.dao.mongo.model.schema.MongoMetadataSpec;
import com.namazustudios.socialengine.dao.mongo.model.schema.MongoTokenTemplate;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.schema.TokenTemplateNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.schema.template.CreateTokenTemplateRequest;
import com.namazustudios.socialengine.model.schema.template.TokenTemplate;
import com.namazustudios.socialengine.model.schema.template.UpdateTokenTemplateRequest;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.*;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;
import static dev.morphia.query.experimental.updates.UpdateOperators.unset;

public class MongoTokenTemplateDao implements TokenTemplateDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    private MongoMetadataSpecDao metadataSpecDao;

    private MongoNeoSmartContractDao smartContractDao;

    private MongoUserDao userDao;

    @Override
    public Pagination<TokenTemplate> getTokenTemplates(final int offset,
                                                     final int count, String userId) {

        var query = getDatastore().find(MongoTokenTemplate.class);

        if (!nullToEmpty(userId).trim().isEmpty()) {
            final var mongoUser = getUserDao().getActiveMongoUser(userId);
            query.filter(eq("user", mongoUser));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, this::transform, new FindOptions());

    }

    @Override
    public TokenTemplate getTokenTemplate(String tokenTemplateIdOrName, String userId) {

        var query = getDatastore().find(MongoTokenTemplate.class);

        if (!nullToEmpty(userId).trim().isEmpty()) {
            final var mongoUser = getUserDao().getActiveMongoUser(userId);
            query.filter(eq("user", mongoUser));
        }
        final var objectId = getMongoDBUtils().parseOrReturnNull(tokenTemplateIdOrName);

        var mongoTokenTemplate = query
                .filter(Filters.or(
                                Filters.eq("_id", objectId),
                                Filters.eq("name", tokenTemplateIdOrName)
                        )
                )
            .first();

        if(mongoTokenTemplate == null) {
            throw new TokenTemplateNotFoundException("Unable to find tokenTemplate with an id of " + tokenTemplateIdOrName);
        }

        return transform(mongoTokenTemplate);
    }

    @Override
    public TokenTemplate updateTokenTemplate(String tokenTemplateId, UpdateTokenTemplateRequest updateTokenTemplateRequest) {
        getValidationHelper().validateModel(updateTokenTemplateRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(tokenTemplateId);
        final var query = getDatastore().find(MongoTokenTemplate.class);

        query.filter(and(
            eq("_id", objectId))
        );

        final var builder = new UpdateBuilder().with(
                set("displayName", updateTokenTemplateRequest.getDisplayName()),
                set("name", updateTokenTemplateRequest.getName())
        );

        if (updateTokenTemplateRequest.getMetadata() != null){
            builder.with(set("metadata", updateTokenTemplateRequest.getMetadata()));
        }else{
            builder.with(unset("metadata"));
        }

        final var metaSpecId = getMongoDBUtils().parseOrReturnNull(updateTokenTemplateRequest.getMetadataSpecId());
        final var spec = getDatastore().find(MongoMetadataSpec.class).filter(and(
                eq("_id", metaSpecId))
        ).first();
        if (spec == null) throw new InvalidDataException("Invalid Metadata Spec.");
        builder.with(set("metadataSpec", spec));

        final var contractId = getMongoDBUtils().parseOrReturnNull(updateTokenTemplateRequest.getContractId());
        final var contract  = getDatastore().find(MongoNeoSmartContract.class).filter(and(
                eq("_id", contractId))
        ).first();
        if (contract == null) throw new InvalidDataException("Invalid Contract.");
        builder.with(set("contract", contract));

        final var userId = getMongoDBUtils().parseOrReturnNull(updateTokenTemplateRequest.getUserId());
        final var user  = getUserDao().getActiveMongoUser(userId);
        if (user == null)   throw new InvalidDataException("Invalid User.");
        builder.with(set("user", user));

        final MongoTokenTemplate mongoTokenTemplate = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoTokenTemplate == null) {
            throw new TokenTemplateNotFoundException("TokenTemplate not found: " + tokenTemplateId);
        }

        getObjectIndex().index(mongoTokenTemplate);
        return transform(mongoTokenTemplate);
    }

    @Override
    public TokenTemplate createTokenTemplate(CreateTokenTemplateRequest createTokenTemplateRequest) {
        getValidationHelper().validateModel(createTokenTemplateRequest, ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoTokenTemplate.class);

        query.filter(exists("name").not());

        final var builder = new UpdateBuilder().with(
                set("displayName", createTokenTemplateRequest.getDisplayName()),
                set("name", createTokenTemplateRequest.getName())
        );

        if (createTokenTemplateRequest.getMetadata() != null){
            builder.with(set("metadata", createTokenTemplateRequest.getMetadata()));
        }

        final var metaSpecId = getMongoDBUtils().parseOrReturnNull(createTokenTemplateRequest.getMetadataSpecId());
        final var spec = getDatastore().find(MongoMetadataSpec.class).filter(and(
                eq("_id", metaSpecId))
        ).first();
        if (spec == null) throw new InvalidDataException("Invalid Metadata Spec.");
        builder.with(set("metadataSpec", spec));

        final var contractId = getMongoDBUtils().parseOrReturnNull(createTokenTemplateRequest.getContractId());
        final var contract  = getDatastore().find(MongoNeoSmartContract.class).filter(and(
                eq("_id", contractId))
        ).first();
        if (contract == null) throw new InvalidDataException("Invalid Contract.");
        builder.with(set("contract", contract));

        final var userId = getMongoDBUtils().parseOrReturnNull(createTokenTemplateRequest.getUserId());
        final var user  = getUserDao().getActiveMongoUser(userId);
        if (user == null)   throw new InvalidDataException("Invalid User.");
        builder.with(set("user", user));

        final var mongoTokenTemplate = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoTokenTemplate);
        return transform(mongoTokenTemplate);
    }

    @Override
    public TokenTemplate cloneTokenTemplate(TokenTemplate tokenTemplate) {
        getValidationHelper().validateModel(tokenTemplate, ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoTokenTemplate.class);

        query.filter(exists("name").not());

        final var builder = new UpdateBuilder().with(
                set("displayName", tokenTemplate.getDisplayName()),
                set("name", tokenTemplate.getName()),
                set("metadataSpec", tokenTemplate.getMetadataSpec()),
                set("contract", tokenTemplate.getContract()),
                set("metadata", tokenTemplate.getMetadata())
        );

        final var mongoTokenTemplate = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoTokenTemplate);
        return transform(mongoTokenTemplate);
    }

    @Override
    public void deleteTokenTemplate(String tokenTemplateId) {
        final var objectId = getMongoDBUtils().parseOrThrow(tokenTemplateId, TokenTemplateNotFoundException::new);

        final var result = getDatastore()
                .find(MongoTokenTemplate.class)
                .filter(eq("_id", objectId))
                .delete();

        if(result.getDeletedCount() == 0){
            throw new TokenTemplateNotFoundException("TokenTemplate not deleted: " + tokenTemplateId);
        }
    }

    private TokenTemplate transform(MongoTokenTemplate input)
    {
        if (!input.getUser().isActive()) {
            input.setUser(null);
        }

        return getBeanMapper().map(input, TokenTemplate.class);
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

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public MongoMetadataSpecDao getMetadataSpecDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MongoMetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }

    public MongoNeoSmartContractDao getSmartContractDao() {
        return smartContractDao;
    }

    @Inject
    public void setSmartContractDao(MongoNeoSmartContractDao smartContractDao) {
        this.smartContractDao = smartContractDao;
    }

    public MongoUserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(MongoUserDao userDao) {
        this.userDao = userDao;
    }
}
