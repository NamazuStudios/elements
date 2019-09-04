package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.ProgressDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.mission.*;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.reward.Reward;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import com.namazustudios.socialengine.model.mission.Step;
import static com.namazustudios.socialengine.model.mission.Step.buildRewardIssuanceTags;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.dao.mongo.model.mission.MongoProgressId.parseOrThrowNotFoundException;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.*;
import static com.namazustudios.socialengine.model.reward.RewardIssuance.Type.*;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

    private MongoMissionDao mongoMissionDao;

    private MongoProfileDao mongoProfileDao;

    private MongoRewardIssuanceDao rewardIssuanceDao;

    @Override
    public Pagination<Progress> getProgresses(final Profile profile, final int offset, final int count,
                                              final List<String> tags)  {
        return getProgresses(profile, offset, count, tags,null);
    }

    @Override
    public Pagination<Progress> getProgresses(final Profile profile, final int offset, final int count,
                                              final List<String> tags, final String search) {
        if (isNotEmpty(nullToEmpty(search).trim())) {
            LOGGER.warn("getProgresss(Profile profile, int offset, int count, String query) was called with a query " +
                        "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);

        query.field("profile").equal(getDozerMapper().map(profile, MongoProfile.class));

        if (tags != null && !tags.isEmpty()) {
            query.field("mission.tags").hasAnyOf(tags);
        }

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, Progress.class));

    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count, List<String> tags)  {
        return getProgresses(offset, count, tags, null);
    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count,
                                              final List<String> tags, final String search) {

        if (isNotEmpty(search)) {
            LOGGER.warn(" getProgresss(int offset, int count, String query) was called with a query " +
                    "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);

        if (tags != null && !tags.isEmpty()) {
            query.field("mission.tags").hasAnyOf(tags);
        }

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, Progress.class));

    }

    @Override
    public Progress getProgressForProfileAndMission(final Profile profile, final String missionNameOrId) {

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);
        final MongoMission mongoMission = getMongoMissionDao().getMongoMissionByNameOrId(missionNameOrId);

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);
        query.field("profile").equal(mongoProfile);
        query.field("_id.missionId").equal(mongoMission.getObjectId());

        final MongoProgress mongoProgress = query.get();

        if (mongoProgress == null) {
            throw new NotFoundException("Progress not found.");
        }

        return getDozerMapper().map(mongoProgress, Progress.class);

    }

    @Override
    public Progress getProgress(final String identifier) {

        if (isEmpty(nullToEmpty(identifier).trim())) {
            throw new NotFoundException("Unable to find progress with an id " + identifier);
        }

        final MongoProgressId mongoProgressId = parseOrThrowNotFoundException(identifier);
        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);

        query.field("_id").equal(mongoProgressId);

        final MongoProgress progress = query.get();

        if (progress == null) {
            throw new NotFoundException("Unable to find item with an id or name of " + identifier);
        }

        return getDozerMapper().map(progress, Progress.class);

    }

    @Override
    public Progress updateProgress(final Progress progress) {

        getValidationHelper().validateModel(progress, Update.class);

        final MongoProgressId mongoProgressId = parseOrThrowNotFoundException(progress.getId());

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);
        query.field("_id").equal(mongoProgressId);

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
    public Progress createOrGetExistingProgress(final Progress progress) {

        getValidationHelper().validateModel(progress, Insert.class);

        normalize(progress);

        final MongoProgress result;

        try {
            result = getMongoConcurrentUtils().performOptimistic(ads -> {

                final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(progress.getProfile());
                final MongoMission mongoMission = getMongoMissionDao().getMongoMissionByNameOrId(progress.getMission().getId());
                final MongoProgressId mongoProgressId = new MongoProgressId(mongoProfile, mongoMission);

                final MongoProgress mongoProgress = getDatastore().get(MongoProgress.class, mongoProgressId);
                if (mongoProgress != null) return mongoProgress;

                final List<Step> steps = progress.getMission().getSteps();
                final Step finalRepeatStep = progress.getMission().getFinalRepeatStep();

                final Step first;

                if (steps == null || steps.isEmpty()) {
                    if (finalRepeatStep == null) throw new InvalidDataException("one step must be defined");
                    first = finalRepeatStep;
                } else {
                    first = steps.get(0);
                }

                progress.setRewardIssuances(emptyList());
                progress.setRemaining(first.getCount());
                getValidationHelper().validateModel(first);

                final MongoProgress toCreate = getDozerMapper().map(progress, MongoProgress.class);
                toCreate.setVersion(randomUUID().toString());

                try {
                    getDatastore().insert(toCreate);
                    return getDatastore().get(toCreate);
                } catch (DuplicateKeyException ex) {
                    throw new ContentionException(ex);
                }

            });
        } catch (MongoConcurrentUtils.ConflictException e) {
            throw new TooBusyException(e);
        }

// TODO: Per SOC-364
// Disabling this For Now SOC-364
//        getObjectIndex().index(result);
        return getDozerMapper().map(result, Progress.class);

    }

    @Override
    public void deleteProgress(final String progressId) {
        final MongoProgressId mongoProgressId = parseOrThrowNotFoundException(progressId);
        final WriteResult writeResult = getDatastore().delete(MongoProgress.class, mongoProgressId);

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

        return getDozerMapper().map(getDatastore().get(mongoProgress), Progress.class);

    }

    private MongoProgress doAdvanceProgress(final Progress progress, final int actionsPerformed) throws ContentionException {

        final MongoProgressId mongoProgressId = parseOrThrowNotFoundException(progress.getId());
        final MongoProgress mongoProgress = getDatastore().get(MongoProgress.class, mongoProgressId);

        final Query<MongoProgress> query = getDatastore().createQuery(MongoProgress.class);

        query.field("_id").equal(mongoProgressId);
        query.field("version").equal(mongoProgress.getVersion());

        if (mongoProgress == null) {
            throw new NotFoundException("Progress with id not found: " + progress.getId());
        }

        final UpdateOperations<MongoProgress> updates = getDatastore().createUpdateOperations(MongoProgress.class);

        if ((progress.getRemaining() - actionsPerformed) > 0) {
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

        int completedSteps = 0;
        int actionsToApply = actionsPerformed;
        int remaining = mongoProgress.getRemaining();
        MongoStep step = mongoProgress.getCurrentStep();

        final MongoUser mongoUser = mongoProgress.getProfile().getUser();

        while (step != null && actionsToApply >= remaining) {

            // We've hit the end of the mission the mission has no final repeat step and has no remaining
            // steps.  Therefore the mission is assumed to be complete.  No further rewards will be issued
            // and this effectively ignores the progress.

            // Assigns the rewards from the step

            final MongoStep _step = step;
            final int _completedSteps = completedSteps;
            final List<MongoRewardIssuance> rewardIssuances = step.getRewards()
                .stream()
                .filter(r -> r != null && r.getItem() != null)
                .map(r -> {
                    final Progress progress = getDozerMapper().map(mongoProgress, Progress.class);
                    final Step __step = getDozerMapper().map(_step, Step.class);
                    final Reward reward = getDozerMapper().map(r, Reward.class);
                    final User user = getDozerMapper().map(mongoUser, User.class);
                    final Map<String, Object> metadata = generateMissionProgressMetadata(progress, __step);

                    final int stepSequence = progress.getSequence() + _completedSteps;

                    final String context = buildMissionProgressContextString(
                            mongoProgress.getObjectId().toHexString(),
                            stepSequence,
                            __step.getRewards().indexOf(reward));

                    final List<String> tags = buildRewardIssuanceTags(progress, stepSequence);

                    final RewardIssuance issuance = new RewardIssuance();
                    issuance.setItem(reward.getItem());
                    issuance.setItemQuantity(reward.getQuantity());
                    issuance.setUser(user);
                    issuance.setType(PERSISTENT);
                    issuance.setSource(MISSION_PROGRESS_SOURCE);
                    issuance.setContext(context);
                    issuance.setMetadata(metadata);
                    issuance.setTags(tags);

                    final RewardIssuance createdRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(issuance);

                    final MongoRewardIssuance mongoRewardIssuance =
                            getDozerMapper().map(createdRewardIssuance, MongoRewardIssuance.class);

                    return mongoRewardIssuance;
                }).collect(toList());

            updates.addToSet("rewardIssuances", rewardIssuances);
            actionsToApply -= remaining;

            // Increments the completed steps and applies to the remaining actions to apply.  We keep
            // repeating this process until we have consumed all actions and assigned all remaining
            // rewards to the Progress.

            ++completedSteps;

            // Determines the current step in the progress
            step = mongoProgress.getStepForSequence(mongoProgress.getSequence() + completedSteps);
            remaining = step == null ? 0 : step.getCount();

        }

        // Advances the remaining fields and then corrects the remaining steps.  If we hit the end of the mission
        // where the Step is simply null, then we set the remaining to zero.  Future iterations of this should
        // skip the mission.

        updates.inc("sequence", completedSteps);
        updates.set("remaining", step == null ? 0 : step.getCount() - actionsToApply);
    }

    public Map<String, Object> generateMissionProgressMetadata(Progress progress, Step step) {
        final Map<String, Object> map = new HashMap<>();
        final Map stepMap = getDozerMapper().map(step, Map.class);

        map.put(MISSION_PROGRESS_PROGRESS_KEY, progress.getId());
        map.put(MISSION_PROGRESS_STEP_KEY, stepMap);

        return map;
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

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

    public MongoMissionDao getMongoMissionDao() {
        return mongoMissionDao;
    }

    @Inject
    public void setMongoMissionDao(MongoMissionDao mongoMissionDao) {
        this.mongoMissionDao = mongoMissionDao;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

    public MongoRewardIssuanceDao getRewardIssuanceDao() {
        return rewardIssuanceDao;
    }

    @Inject
    public void setRewardIssuanceDao(MongoRewardIssuanceDao rewardIssuanceDao) {
        this.rewardIssuanceDao = rewardIssuanceDao;
    }

}
