package dev.getelements.elements.dao.mongo.largeobject;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.largeobject.MongoLargeObject;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.and;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

public class MongoLargeObjectDao implements LargeObjectDao {

    private ValidationHelper validationHelper;
    private MongoDBUtils mongoDBUtils;
    private Datastore datastore;
    private Mapper dozerMapper;

    public MongoLargeObjectDao() {
    }

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        if (!ObjectId.isValid(objectId)) return Optional.empty();

        final var query = getDatastore().find(MongoLargeObject.class);
        query.filter(and(
                eq("_id", new ObjectId(objectId))
        ));

        return Optional.ofNullable(query.first()).map(this::transform);
    }

    @Override
    public LargeObject createLargeObject(final LargeObject largeObject) {
        getValidationHelper().validateModel(largeObject, ValidationGroups.Insert.class);

        MongoLargeObject mongoLargeObject = transform(largeObject);

        try {
            getDatastore().insert(mongoLargeObject);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        return getLargeObject(mongoLargeObject.getId().toHexString());
    }

    @Override
    public LargeObject updateLargeObject(final LargeObject largeObject) {
        getValidationHelper().validateModel(largeObject, ValidationGroups.Update.class);

        final var query = getDatastore().find(MongoLargeObject.class);

        final var mongoLargeObject = mongoDBUtils.perform(ds ->
                query.modify(
                        set("mimeType", largeObject.getMimeType()),
                        set("url", largeObject.getUrl()),
                        set("path", largeObject.getPath()),
                        set("accessPermissions", largeObject.getAccessPermissions())
                ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return transform(mongoLargeObject);
    }

    //TODO: flag it as delete with exp date (in future)
    @Override
    public LargeObject deleteLargeObject(final String objectId) {
        final var query = getDatastore().find(MongoLargeObject.class);
        final LargeObject result = getLargeObject(objectId);

        query.filter(and(
                eq("_id", new ObjectId(objectId))
        ));

        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("LargeObject not found: " + objectId);
        } else if (deleteResult.getDeletedCount() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }

        return result;
    }

    private LargeObject transform(final MongoLargeObject mongoLargeObject) {
        return getDozerMapper().map(mongoLargeObject, LargeObject.class);
    }

    private MongoLargeObject transform(final LargeObject largeObject) {
        return getDozerMapper().map(largeObject, MongoLargeObject.class);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
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

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }
}
