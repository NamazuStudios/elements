package dev.getelements.elements.dao.mongo.schema;

import dev.getelements.elements.dao.MetadataSpecDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.exception.schema.MetadataSpecNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.schema.template.CreateMetadataSpecRequest;
import dev.getelements.elements.model.schema.template.MetadataSpec;
import dev.getelements.elements.model.schema.template.UpdateMetadataSpecRequest;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoMetadataSpecDao implements MetadataSpecDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<MetadataSpec> getMetadataSpecs(final int offset,
                                                     final int count) {

        final var mongoQuery = getDatastore().find(MongoMetadataSpec.class);

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform, new FindOptions());

    }

    @Override
    public MetadataSpec getMetadataSpec(String metadataSpecIdOrName) {

        final var objectId = getMongoDBUtils().parseOrReturnNull(metadataSpecIdOrName);

        var mongoTokenTemplate = getDatastore()
            .find(MongoMetadataSpec.class)
                .filter(Filters.or(
                                Filters.eq("_id", objectId),
                                Filters.eq("name", metadataSpecIdOrName)
                        )
                )
            .first();

        if(mongoTokenTemplate == null) {
            throw new MetadataSpecNotFoundException("Unable to find metadataSpec with an id of " + metadataSpecIdOrName);
        }

        return transform(mongoTokenTemplate);
    }

    @Override
    public MetadataSpec updateMetadataSpec(String metadataSpecId, UpdateMetadataSpecRequest updateMetadataSpecRequest) {
        getValidationHelper().validateModel(updateMetadataSpecRequest, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(metadataSpecId);
        final var query = getDatastore().find(MongoMetadataSpec.class);

        query.filter(and(
            eq("_id", objectId))
        );

        final var builder = new UpdateBuilder().with(
            set("tabs", updateMetadataSpecRequest.getTabs()),
                set("name", updateMetadataSpecRequest.getName())
        );

        final MongoMetadataSpec mongoMetadataSpec = getMongoDBUtils().perform(ds ->
                builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoMetadataSpec == null) {
            throw new MetadataSpecNotFoundException("MetadataSpec not found or was already minted: " + metadataSpecId);
        }

        return transform(mongoMetadataSpec);
    }

    @Override
    public MetadataSpec createMetadataSpec(CreateMetadataSpecRequest createMetadataSpecRequest) {
        getValidationHelper().validateModel(createMetadataSpecRequest, ValidationGroups.Insert.class);
        getValidationHelper().validateModel(createMetadataSpecRequest.getTabs(), ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoMetadataSpec.class);

        query.filter(exists("name").not());

        final var builder = new UpdateBuilder().with(
            set("tabs", createMetadataSpecRequest.getTabs()),
                set("name", createMetadataSpecRequest.getName())
        );

        final var mongoTokenTemplate = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return transform(mongoTokenTemplate);
    }

    @Override
    public MetadataSpec cloneMetadataSpec(MetadataSpec metadataSpec) {
        getValidationHelper().validateModel(metadataSpec.getTabs(), ValidationGroups.Insert.class);

        final var query = getDatastore().find(MongoMetadataSpec.class);
        final var tabs = metadataSpec.getTabs();

        query.filter(exists("name").not());

        final var builder = new UpdateBuilder().with(
                set("tab", tabs),
                set("name", metadataSpec.getName())
        );

        final var mongoTokenTemplate = getMongoDBUtils().perform(
                ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return transform(mongoTokenTemplate);
    }

    @Override
    public void deleteMetadataSpec(String metadataSpecId) {
        final var objectId = getMongoDBUtils().parseOrThrow(metadataSpecId, MetadataSpecNotFoundException::new);

        final var result = getDatastore()
                .find(MongoMetadataSpec.class)
                .filter(eq("_id", objectId))
                .delete();

        if(result.getDeletedCount() == 0){
            throw new MetadataSpecNotFoundException("MetadataSpec not deleted: " + metadataSpecId);
        }
    }

    private MetadataSpec transform(MongoMetadataSpec token)
    {
        return getBeanMapper().map(token, MetadataSpec.class);
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

}
