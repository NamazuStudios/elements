package dev.getelements.elements.dao.mongo.schema;

import dev.getelements.elements.dao.MetadataSpecDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.exception.schema.MetadataSpecNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.exists;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoMetadataSpecDao implements MetadataSpecDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<MetadataSpec> getActiveMetadataSpecs(final int offset, final int count) {

        final var mongoQuery = getDatastore()
                .find(MongoMetadataSpec.class)
                .filter(eq("active", true), exists("name"));

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform);

    }

    @Override
    public Optional<MetadataSpec> findActiveMetadataSpec(final String metadataSpecId) {
        return findActiveMongoMetadataSpec(metadataSpecId).map(this::transform);
    }

    public Optional<MongoMetadataSpec> findActiveMongoMetadataSpec(final String metadataSpecId) {
        return getMongoDBUtils()
                .parse(metadataSpecId)
                .map(objectId ->  getDatastore()
                        .find(MongoMetadataSpec.class)
                        .filter(eq("_id", objectId), eq("active", true))
                        .first()
                );
    }

    @Override
    public Optional<MetadataSpec> findActiveMetadataSpecByName(final String metadataSpecName) {
        return findActiveMongoMetadataSpecByName(metadataSpecName).map(this::transform);
    }

    public Optional<MongoMetadataSpec> findActiveMongoMetadataSpecByName(final String metadataSpecName) {

        final var spec = getDatastore()
                .find(MongoMetadataSpec.class)
                .filter(eq("name", metadataSpecName), eq("active", true))
                .first();

        return Optional.ofNullable(spec);

    }


    @Override
    public MetadataSpec createMetadataSpec(final MetadataSpec metadataSpec) {

        getValidationHelper().validateModel(metadataSpec, ValidationGroups.Insert.class);

        final var toInsert = getBeanMapper().map(metadataSpec, MongoMetadataSpec.class);
        toInsert.setActive(true);

        final var inserted = getMongoDBUtils().perform(ds -> ds.save(toInsert));
        return getBeanMapper().map(inserted, MetadataSpec.class);

    }

    @Override
    public MetadataSpec updateActiveMetadataSpec(final MetadataSpec metadataSpec) {

        getValidationHelper().validateModel(metadataSpec, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parse(metadataSpec.getId());

        if (objectId.isEmpty()) {
            throw new MetadataSpecNotFoundException("Spec with id not found.");
        }

        final var mongoMetadataSpec = getBeanMapper().map(metadataSpec, MongoMetadataSpec.class);

        final var options = new ModifyOptions()
                .upsert(false)
                .returnDocument(AFTER);

        final var updated = getDatastore().find(MongoMetadataSpec.class)
                .filter(eq("_id", objectId.get()), eq("active", true))
                .modify(options,
                        set("name", metadataSpec.getName()),
                        set("properties", mongoMetadataSpec.getProperties()),
                        set("type", mongoMetadataSpec.getType())
                );

        if (updated == null) {
            throw new MetadataSpecNotFoundException("Spec with id not found.");
        }

        return transform(mongoMetadataSpec);


    }

    @Override
    public void deleteMetadataSpec(final String metadataSpecId) {
        if (findActiveMongoMetadataSpec(metadataSpecId).isEmpty()) {
            throw new MetadataSpecNotFoundException("No such metadata spec: " + metadataSpecId);
        }

        final var objectId = getMongoDBUtils().parseOrThrow(metadataSpecId, MetadataSpecNotFoundException::new);

        final var options = new UpdateOptions().upsert(false);

        final var result = getDatastore()
                .find(MongoMetadataSpec.class)
                .filter(eq("_id", objectId))
                .update(options,
                        set("active", false),
                        set("name", UUID.randomUUID().toString())); //name must be randomized

    }

    public MetadataSpec transform(final MongoMetadataSpec mongoMetadataSpec) {
        return getBeanMapper().map(mongoMetadataSpec, MetadataSpec.class);
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
