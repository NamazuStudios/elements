package com.namazustudios.socialengine.dao.mongo.blockchain;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.TokenTemplateDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.UpdateBuilder;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoTokenTemplate;
import com.namazustudios.socialengine.exception.blockchain.TokenTemplateNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.template.CreateTokenTemplateRequest;
import com.namazustudios.socialengine.model.blockchain.template.TokenTemplate;
import com.namazustudios.socialengine.model.blockchain.template.UpdateTokenTemplateRequest;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.*;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoTokenTemplateDao implements TokenTemplateDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<TokenTemplate> getTokenTemplates(final int offset,
                                               final int count) {

        final var mongoQuery = getDatastore().find(MongoTokenTemplate.class);

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform, new FindOptions());

    }

    @Override
    public TokenTemplate getTokenTemplate(String templateId) {

        final var objectId = getMongoDBUtils().parseOrReturnNull(templateId);

        var mongoTokenTemplate = getDatastore()
            .find(MongoTokenTemplate.class)
            .filter(Filters.eq("_id", objectId))
            .first();

        if(mongoTokenTemplate == null) {
            throw new TokenTemplateNotFoundException("Unable to find token template with an id of " + templateId);
        }

        return transform(mongoTokenTemplate);
    }

    @Override
    public TokenTemplate updateTokenTemplate(String tokenId, UpdateTokenTemplateRequest updateTokenTemplateRequest) {
        getValidationHelper().validateModel(updateTokenTemplateRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);
        final var query = getDatastore().find(MongoTokenTemplate.class);

        query.filter(and(
            eq("_id", objectId))
        );

        final var builder = new UpdateBuilder().with(
            set("tabs", updateTokenTemplateRequest.getTabs())
        );

        final MongoTokenTemplate mongoTokenTemplate = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoTokenTemplate == null) {
            throw new TokenTemplateNotFoundException("TokenTemplate not found or was already minted: " + tokenId);
        }

        getObjectIndex().index(mongoTokenTemplate);
        return transform(mongoTokenTemplate);
    }

    @Override
    public TokenTemplate createTokenTemplate(CreateTokenTemplateRequest tokenRequest) {
        getValidationHelper().validateModel(tokenRequest, ValidationGroups.Insert.class);
        getValidationHelper().validateModel(tokenRequest.getTabs(), ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoTokenTemplate.class);

        query.filter(exists("tabs").not());

        final var builder = new UpdateBuilder().with(
            set("tabs", tokenRequest.getTabs())
        );

        final var mongoTokenTemplate = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoTokenTemplate);
        return transform(mongoTokenTemplate);
    }

    @Override
    public TokenTemplate cloneTokenTemplate(TokenTemplate tokenTemplate) {
        getValidationHelper().validateModel(tokenTemplate.getTabs(), ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoTokenTemplate.class);
        final var tabs = tokenTemplate.getTabs();

        final var builder = new UpdateBuilder().with(
                set("tab", tabs)
        );

        final var mongoTokenTemplate = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        getObjectIndex().index(mongoTokenTemplate);
        return transform(mongoTokenTemplate);
    }

    @Override
    public void deleteTokenTemplate(String tokenId) {
        final var objectId = getMongoDBUtils().parseOrThrow(tokenId, TokenTemplateNotFoundException::new);

        final var result = getDatastore()
                .find(MongoTokenTemplate.class)
                .filter(eq("_id", objectId))
                .delete();

        if(result.getDeletedCount() == 0){
            throw new TokenTemplateNotFoundException("TokenTemplate not deleted: " + tokenId);
        }
    }

    private TokenTemplate transform(MongoTokenTemplate token)
    {
        return getBeanMapper().map(token, TokenTemplate.class);
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

}
