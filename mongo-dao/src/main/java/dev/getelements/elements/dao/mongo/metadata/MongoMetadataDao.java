package dev.getelements.elements.dao.mongo.metadata;

import dev.getelements.elements.sdk.dao.MetadataDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.metadata.MongoMetadata;
import dev.getelements.elements.sdk.model.exception.metadata.MetadataNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import dev.morphia.query.Query;
import jakarta.inject.Inject;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoMetadataDao implements MetadataDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapperRegistry;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<Metadata> getMetadatas(final int offset, final int count, final User.Level accessLevel) {

        final var mongoQuery = getDatastore()
                .find(MongoMetadata.class)
                .filter(exists("name"));

        final var filteredQuery = filterQueryByAccessLevel(mongoQuery, accessLevel);

        return getMongoDBUtils().paginationFromQuery(filteredQuery, offset, count, this::transform);

    }

    @Override
    public Pagination<Metadata> searchMetadatas(int offset, int count, final String search, final User.Level accessLevel) {
        final var mongoQuery = getDatastore()
                .find(MongoMetadata.class)
                .filter(exists("name"));

        final var filteredQuery = filterQueryByAccessLevel(mongoQuery, accessLevel);

        return getMongoDBUtils().paginationFromQuery(filteredQuery, offset, count, this::transform);
    }

    @Override
    public Optional<Metadata> findMetadata(final String metadataId, final User.Level accessLevel) {
        return findMongoMetadata(metadataId, accessLevel).map(this::transform);
    }

    public Optional<MongoMetadata> findMongoMetadata(final String metadataId, final User.Level accessLevel) {
        return getMongoDBUtils()
                .parse(metadataId)
                .map(objectId -> {

                    final var metadata = getDatastore()
                            .find(MongoMetadata.class)
                            .filter(eq("_id", objectId), exists("name"));

                    return filterQueryByAccessLevel(metadata, accessLevel).first();
                });
    }

    @Override
    public Optional<Metadata> findMetadataByName(final String metadataName, final User.Level accessLevel) {
        return findMongoMetadataByName(metadataName, accessLevel).map(this::transform);
    }

    public Optional<MongoMetadata> findMongoMetadataByName(final String metadataName, final User.Level accessLevel) {

        final var metadata = getDatastore()
                .find(MongoMetadata.class)
                .filter(eq("name", metadataName));

        final var filteredQuery = filterQueryByAccessLevel(metadata, accessLevel).first();

        return Optional.ofNullable(filteredQuery);
    }

    @Override
    public Metadata createMetadata(final Metadata metadata) {
        getValidationHelper().validateModel(metadata, ValidationGroups.Insert.class);
        final var toInsert = getBeanMapper().map(metadata, MongoMetadata.class);
        final var inserted = getMongoDBUtils().perform(ds -> ds.save(toInsert));
        return getBeanMapper().map(inserted, Metadata.class);
    }

    @Override
    public Metadata updateMetadata(final Metadata metadata) {

        getValidationHelper().validateModel(metadata, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(
                metadata.getId(),
                MetadataNotFoundException::new
        );

        final var mongoMetadata = getBeanMapper().map(metadata, MongoMetadata.class);

        final var options = new ModifyOptions()
                .upsert(false)
                .returnDocument(AFTER);

        final var updated = getDatastore().find(MongoMetadata.class)
                .filter(eq("_id", objectId), exists("name"))
                .modify(options,
                        (mongoMetadata.getMetadataSpec() == null ? unset("spec") : set("spec", mongoMetadata.getMetadataSpec())),
                        set("metadata", mongoMetadata.getMetadata()),
                        set("accessLevel", metadata.getAccessLevel())
                );

        if (updated == null) {
            throw new MetadataNotFoundException("Metadata with id not found.");
        }

        return transform(updated);
    }

    @Override
    public void softDeleteMetadata(final String metadataId) {

        final var objectId = getMongoDBUtils().parseOrThrow(metadataId, MetadataNotFoundException::new);

        final var query = getDatastore()
                .find(MongoMetadata.class)
                .filter(
                        exists("name"),
                        eq("_id", objectId)
                );

        final var result = new UpdateBuilder()
                .with(unset("name"))
                .execute(query, new UpdateOptions().upsert(false));

        if (result.getModifiedCount() == 0) {
            throw new MetadataNotFoundException();
        }

    }

    public Metadata transform(final MongoMetadata mongoMetadata) {
        return getBeanMapper().map(mongoMetadata, Metadata.class);
    }

    private <T> Query<T> filterQueryByAccessLevel(final Query<T> query, final User.Level accessLevel) {
        switch (accessLevel) {
            case USER -> {
                return query.filter(
                        ne("accessLevel", User.Level.SUPERUSER.toString())
                );
            }
            case UNPRIVILEGED -> {
                return query.filter(
                        eq("accessLevel", User.Level.UNPRIVILEGED.toString())
                );
            }
            default -> {return query;}
        }
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
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
