package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.ProgressDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoPendingReward;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoProgress;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoStep;
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
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

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
    public Progress getProgress(final String identifier) {
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
    public Progress updateProgress(final Progress progress) {

        getValidationHelper().validateModel(progress, Update.class);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(progress.getId());

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);
        query.criteria("_id").equal(objectId);

        final UpdateOperations<MongoProgress> operations = getDatastore().createUpdateOperations(MongoProgress.class);

        operations.set("version", randomUUID().toString());
        operations.set("remaining", progress.getRemaining());
        operations.set("currentStep", progress.getCurrentStep());

        final FindAndModifyOptions options = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoProgress updatedMongoProgress = getDatastore().findAndModify(query, operations, options);

        if (updatedMongoProgress == null) {
            throw new NotFoundException("Progress with id or name of " + progress.getId() + " does not exist");
        }

        getObjectIndex().index(updatedMongoProgress);

        return getDozerMapper().map(updatedMongoProgress, Progress.class);
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
    public Progress advanceProgress(final Progress progress, final int actionsPerformed) {

        final MongoProgress mongoProgress;

        try {
            mongoProgress = getMongoConcurrentUtils().performOptimistic(ads -> doAdvanceProgress(progress, actionsPerformed));
        } catch (MongoConcurrentUtils.ConflictException e) {
            throw new TooBusyException(e);
        }

        // To finalize the advancement, we clear the expiry of hte rewards.  The rationale here is if extra orphaned
        // pending rewards exist, then the will be eventually cleared by the database.  However, sicne the actual
        // MongoProress tracks the object IDs we will still have a consistent view even if orphans exist.  The expiry
        // on the MongoPending reward is just to provide a means of garbage collecting unreferenced rewards.
        final Query<MongoPendingReward> query = getDatastore().createQuery(MongoPendingReward.class);
        query.field("progress").equal(mongoProgress);

        final UpdateOperations<MongoPendingReward> updates = getDatastore().createUpdateOperations(MongoPendingReward.class);
        updates.unset("expires");

        getDatastore().update(query, updates, new UpdateOptions().multi(true).upsert(false));

        return getDozerMapper().map(mongoProgress, Progress.class);

    }

    private MongoProgress doAdvanceProgress(final Progress progress, final int actionsPerformed) throws ContentionException {

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

        if (actionsPerformed < progress.getRemaining()) {
            updates.dec("remaining", actionsPerformed);
        } else {
            advanceMission(updates, mongoProgress, actionsPerformed);
        }

        final MongoProgress result = getDatastore().findAndModify(query, updates, new FindAndModifyOptions()
            .upsert(false)
            .returnNew(true));

        if (result == null) {
            // This happens because either the Progress was deleted while applying the rewards, the version mismatched
            // indicating that another process beat us to writing this to the database.  If it was deleted, the next
            // go around will get the NotFoundException above.
            throw new ContentionException();
        }

        return result;

    }

    private void advanceMission(final UpdateOperations<MongoProgress> updates,
                                final MongoProgress mongoProgress,
                                final int actionsPerformed) {

        MongoStep step;
        int completedSteps = 0;
        int actionsToApply = actionsPerformed;

        do {

            // Determines the current step in the progress
            step = mongoProgress.getStepForSequence(mongoProgress.getSequence() + completedSteps);

            // We've hit the end of the mission the mission has no final repeat step and has no remaining
            // steps.  Therefore the mission is assumed to be complete.  No further rewards will be issued
            // and this effectively ignores the progress.

            if (step == null) break;

            // Assigns the rewards from the step

            final List<MongoPendingReward> pendingRewards = step.getRewards()
                .stream()
                .filter(r -> r != null && r.getItem() != null)
                .map(r -> {
                    final MongoPendingReward pending = new MongoPendingReward();
                    pending.setReward(r);
                    pending.setObjectId(new ObjectId());
                    pending.setProgress(mongoProgress);
                    pending.setExpires(new Timestamp(currentTimeMillis()));
                    getDatastore().insert(pending);
                    return pending;
                }).collect(toList());

            updates.push("pendingRewards", pendingRewards);

            // Increments the completed steps and applies to the remaining actions to apply.  We keep
            // repeating this process until we have consumed all actions and assigned all remaining
            // rewards to the Progress.

            ++completedSteps;
            actionsToApply -= step.getCount();

        } while (actionsToApply > 0);

        // Advances the remaining fields and then corrects the remaining steps.  If we hit the end of the mission
        // where the Step is simply null, then we set the remaining to zero.  Future iterations of this should
        // skip the mission.

        updates.inc("sequence", completedSteps);
        updates.set("remaining", step == null ? 0 : step.getCount() + actionsToApply);

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
