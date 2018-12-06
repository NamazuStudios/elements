package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.ProgressDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoProgress;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class MongoProgressDao implements ProgressDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoProgressDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public Pagination<Progress> getProgresses(Profile profile, int offset, int count)  { return getProgresses(profile, offset, count, null); }

    @Override
    public Pagination<Progress> getProgresses(Profile profile, int offset, int count, String search) {
        if (StringUtils.isNotEmpty(search)) {
            LOGGER.warn(" getProgresss(Profile profile, int offset, int count, String query) was called with a query " +
                    "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);

        query.criteria("profile").equal(getDozerMapper().map(profile, MongoProfile.class));

        return getMongoDBUtils().paginationFromQuery(query, offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, Progress.class));

    }

    @Override
    public Pagination<Progress> getProgresses(int offset, int count)  { return getProgresses(offset, count, null); }

    @Override
    public Pagination<Progress> getProgresses(int offset, int count, String search) {
        if (StringUtils.isNotEmpty(search)) {
            LOGGER.warn(" getProgresss(int offset, int count, String query) was called with a query " +
                    "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);

        return getMongoDBUtils().paginationFromQuery(query, offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, Progress.class));

    }

    @Override
    public Progress getProgress(String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            throw new NotFoundException("Unable to find progress with an id " + identifier);
        }

        Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);

        if (ObjectId.isValid(identifier)) {
            query.criteria("_id").equal(new ObjectId(identifier));
        } else {
            query.criteria("name").equal(identifier);
        }

        final MongoProgress progress = query.get();

        if (progress == null) {
            throw new NotFoundException("Unable to find item with an id or name of " + identifier);
        }

        return getDozerMapper().map(progress, Progress.class);
    }

    @Override
    public Progress updateProgress(Progress progress) {
        getValidationHelper().validateModel(progress, Update.class);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(progress.getId());

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);
        query.criteria("_id").equal(objectId);

        final UpdateOperations<MongoProgress> operations = getDatastore().createUpdateOperations(MongoProgress.class);
        operations.set("currentStep", progress.getCurrentStep());
        operations.set("remaining", progress.getRemaining());

        final FindAndModifyOptions options = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoProgress updatedMongoItem = getDatastore().findAndModify(query, operations, options);
        if (updatedMongoItem == null) {
            throw new NotFoundException("Progress with id or name of " + progress.getId() + " does not exist");
        }

        getObjectIndex().index(updatedMongoItem);

        return getDozerMapper().map(updatedMongoItem, Progress.class);
    }

    @Override
    public Progress createProgress(Progress progress) {
        getValidationHelper().validateModel(progress, Insert.class);

        normalize(progress);

        final MongoProgress mongoItem = getDozerMapper().map(progress, MongoProgress.class);

        try {
            getDatastore().save(mongoItem);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }
        getObjectIndex().index(mongoItem);

        return getDozerMapper().map(getDatastore().get(mongoItem), Progress.class);
    }

    @Override
    public void deleteProgress(String progressId) {
        final ObjectId id = getMongoDBUtils().parseOrThrowNotFoundException(progressId);
        final WriteResult writeResult = getDatastore().delete(MongoProgress.class, id);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Progress not found: " + progressId);
        }
    }


    private void normalize(Progress item) {
        // leave this stub here in case we implement some normalization logic later
    }

    public AdvancedDatastore getDatastore() {

        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {

        this.datastore = datastore;
    }

    public Mapper getDozerMapper() {

        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {

        this.dozerMapper = dozerMapper;
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


    public StandardQueryParser getStandardQueryParser() {

        return standardQueryParser;
    }

    @Inject
    public void setStandardQueryParser(StandardQueryParser standardQueryParser) {
        this.standardQueryParser = standardQueryParser;
    }

    public ObjectIndex getObjectIndex() {

        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {

        this.objectIndex = objectIndex;
    }

}
