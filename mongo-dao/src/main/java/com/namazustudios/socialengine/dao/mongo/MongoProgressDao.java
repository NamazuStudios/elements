package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.ProgressDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoProgress;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.mission.Mission;
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
import javax.inject.Singleton;
import java.util.List;

import static java.util.UUID.randomUUID;

@Singleton
public class MongoProgressDao implements ProgressDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoProgressDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private MongoConcurrentUtils mongoConcurrentUtils;

    @Override
    public Pagination<Progress> getProgresses(Profile profile, int offset, int count)  {
        return getProgresses(profile, offset, count, null);
    }

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
    public List<Progress> getProgressesForProfileAndMission(Profile profile, Mission mission) {
        return null;
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

        final MongoProgress mongoProgress = getDozerMapper().map(progress, MongoProgress.class);
        mongoProgress.setVersion(randomUUID().toString());

        try {
            getDatastore().save(mongoProgress);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        getObjectIndex().index(mongoProgress);
        return getDozerMapper().map(getDatastore().get(mongoProgress), Progress.class);

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

    @Override
    public Progress advanceProgress(final Progress progress, final int amount) {
        try {
            return getMongoConcurrentUtils().performOptimistic(ads -> doAdvanceProgress(progress, amount));
        } catch (MongoConcurrentUtils.ConflictException e) {
            throw new TooBusyException(e);
        }
    }

    private Progress doAdvanceProgress(final Progress progress, final int amount) throws ContentionException {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(progress.getId());
        final MongoProgress mongoProgress = getDatastore().get(MongoProgress.class, objectId);

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);

        query.field("_id").equal(objectId);
        query.field("version").equal(mongoProgress.getVersion());

        if (mongoProgress == null) {
            throw new NotFoundException("Progress with id not found: " + progress.getId());
        }

        final UpdateOperations<MongoProgress> updates = getDatastore().createUpdateOperations(MongoProgress.class);
        updates.set("version", randomUUID().toString());

        final int remaining = progress.getRemaining() - amount;
        // TODO: Calculate Prizes and apply.

        return getDozerMapper().map(mongoProgress, Progress.class);

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

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

}
