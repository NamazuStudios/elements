package dev.getelements.elements.dao.mongo.metadata;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoCommandException;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.metadata.MongoMetadata;
import dev.getelements.elements.dao.mongo.schema.MongoMetadataSpecDao;
import dev.getelements.elements.sdk.dao.MetadataDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.ConflictException;
import dev.getelements.elements.sdk.model.exception.metadata.MetadataNotFoundException;
import dev.getelements.elements.sdk.model.exception.schema.MetadataSpecNotFoundException;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoMetadataDao implements MetadataDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapper;

    private ValidationHelper validationHelper;

    private MongoMetadataSpecDao mongoMetadataSpecDao;

    @Override
    public List<Metadata> getAllMetadatasBySpec(final String metadataSpecNameOrId) {
        return getMongoMetadataSpecDao()
                .findActiveMongoMetadataSpec(metadataSpecNameOrId)
                .map(metadataSpec -> getDatastore()
                    .find(MongoMetadata.class)
                    .filter(eq("metadataSpec", metadataSpec), exists("name"))
                    .stream()
                    .map(this::transform)
                    .toList()
                )
                .orElseGet(List::of);
    }

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

        final String trimmedQueryString = nullToEmpty(search).trim();

        if (!trimmedQueryString.isEmpty()) {
            filteredQuery.filter(
                    or(
                            Filters.regex("name", Pattern.compile(search)),
                            Filters.regex("accessLevel", Pattern.compile(search))
                    )
            );
        }

        return getMongoDBUtils().paginationFromQuery(filteredQuery, offset, count, this::transform);
    }

    @Override
    public Optional<Metadata> findMetadata(final String metadataId, final User.Level accessLevel) {
        return findMongoMetadata(metadataId, accessLevel).map(this::transform);
    }

    public Optional<MongoMetadata> findMongoMetadata(final String metadataId, final User.Level accessLevel) {

        final var query = getDatastore().find(MongoMetadata.class);

        if (ObjectId.isValid(metadataId)) {
            query.filter(eq("_id", new ObjectId(metadataId)), exists("name"));
        } else {
            query.filter(eq("name", metadataId));
        }

        final var result = filterQueryByAccessLevel(query, accessLevel).first();

        return Optional.ofNullable(result);
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

        try {
            final var updated = getDatastore().find(MongoMetadata.class)
                    .filter(eq("_id", objectId), exists("name"))
                    .modify(options,
                            (mongoMetadata.getMetadataSpec() == null ? unset("metadataSpec") : set("metadataSpec", mongoMetadata.getMetadataSpec())),
                            set("name", mongoMetadata.getName()),
                            set("metadata", mongoMetadata.getMetadata()),
                            set("accessLevel", mongoMetadata.getAccessLevel())
                    );

            if (updated == null) {
                throw new MetadataNotFoundException("Metadata with id not found.");
            }

            return transform(updated);

        } catch (MongoCommandException ex) {
            throw new ConflictException("Metadata with name " + metadata.getName() + " already exists.");
        }
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

    public MongoMetadataSpecDao getMongoMetadataSpecDao() {
        return mongoMetadataSpecDao;
    }

    @Inject
    public void setMongoMetadataSpecDao(final MongoMetadataSpecDao mongoMetadataSpecDao) {
        this.mongoMetadataSpecDao = mongoMetadataSpecDao;
    }

}
