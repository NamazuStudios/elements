package dev.getelements.elements.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.result.DeleteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import dev.getelements.elements.dao.ProgressDao;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils.ContentionException;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.mission.*;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.exception.TooBusyException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Update;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.reward.Reward;
import dev.getelements.elements.model.reward.RewardIssuance;
import dev.getelements.elements.model.mission.Step;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.model.mission.Step.buildRewardIssuanceTags;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static com.google.common.base.Strings.nullToEmpty;
import static dev.getelements.elements.dao.mongo.model.mission.MongoProgressId.parseOrThrowNotFoundException;
import static dev.getelements.elements.model.reward.RewardIssuance.*;
import static dev.getelements.elements.model.reward.RewardIssuance.Type.*;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.*;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Singleton
public class MongoProgressDao implements ProgressDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoProgressDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private Datastore datastore;

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
            logger.warn("getProgresss(Profile profile, int offset, int count, String query) was called with a query " +
                        "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoProgress> query = getDatastore().find(MongoProgress.class);

        query.filter(eq("profile", getDozerMapper().map(profile, MongoProfile.class)));

        if (tags != null && !tags.isEmpty()) {
            query.filter(Filters.in("mission.tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, Progress.class), new FindOptions());

    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count, List<String> tags)  {
        return getProgresses(offset, count, tags, null);
    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count,
                                              final List<String> tags, final String search) {

        if (isNotEmpty(search)) {
            logger.warn(" getProgresss(int offset, int count, String query) was called with a query " +
                    "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoProgress> query = getDatastore().find(MongoProgress.class);

        if (tags != null && !tags.isEmpty()) {
            query.filter(Filters.in("mission.tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(
            query, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, Progress.class), new FindOptions());

    }

    @Override
    public Progress getProgressForProfileAndMission(final Profile profile, final String missionNameOrId) {

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);
        final MongoMission mongoMission = getMongoMissionDao().getMongoMissionByNameOrId(missionNameOrId);

        final Query<MongoProgress> query = getDatastore().find(MongoProgress.class);
        query.filter(eq("profile", mongoProfile));
        query.filter(eq("_id.missionId", mongoMission.getObjectId()));

        final MongoProgress mongoProgress = query.first();

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
        final Query<MongoProgress> query = getDatastore().find(MongoProgress.class);

        query.filter(eq("_id", mongoProgressId));

        final MongoProgress progress = query.first();

        if (progress == null) {
            throw new NotFoundException("Unable to find item with an id or name of " + identifier);
        }

        return getDozerMapper().map(progress, Progress.class);

    }

    @Override
    public Progress updateProgress(final Progress progress) {

        getValidationHelper().validateModel(progress, Update.class);

        final var mongoProgressId = parseOrThrowNotFoundException(progress.getId());

        final var query = getDatastore().find(MongoProgress.class);
        query.filter(eq("_id", mongoProgressId));

        final var mongoProgress = query.modify(
            set("version", randomUUID().toString()),
            set("remaining", progress.getRemaining()),
            set("currentStep", progress.getCurrentStep())
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (mongoProgress == null) {
            throw new NotFoundException("Progress with id or name of " + progress.getId() + " does not exist");
        }

        getObjectIndex().index(mongoProgress);
        return getDozerMapper().map(mongoProgress, Progress.class);

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

                final Query<MongoProgress> query = getDatastore().find(MongoProgress.class);
                query.filter(eq("_id", mongoProgressId));
                final MongoProgress mongoProgress = query.first();
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
                    return toCreate;
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
        final Query<MongoProgress> query = getDatastore().find(MongoProgress.class);
        query.filter(eq("_id", mongoProgressId));
        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
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

        return getDozerMapper().map(mongoProgress, Progress.class);

    }

    private MongoProgress doAdvanceProgress(final Progress progress, final int actionsPerformed) throws ContentionException {

        final var mongoProgressId = parseOrThrowNotFoundException(progress.getId());

        final var query = getDatastore().find(MongoProgress.class);
        query.filter(eq("_id", mongoProgressId));

        final var mongoProgress = query.first();

        if (mongoProgress == null) throw new NotFoundException("Progress with id not found: " + progress.getId());
        query.filter(eq("version", mongoProgress.getVersion()));

        final var result = progress.getRemaining() - actionsPerformed > 0
                ? debitActions(query, actionsPerformed)
                : advanceMission(query, mongoProgress, actionsPerformed);

        if (result == null) {
            // This happens because either the Progress was deleted while applying the rewards, the version mismatched
            // indicating that another process beat us to writing this to the database.  If it was deleted, the next
            // go around will get the NotFoundException above.
            throw new ContentionException();
        }

        return result;

    }

    private MongoProgress debitActions(final Query<MongoProgress> query,
                                       final int actionsPerformed) {
        return query.modify(
            set("version", randomUUID().toString()),
            dec("remaining", actionsPerformed)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));
    }

    private MongoProgress advanceMission(final Query<MongoProgress> query,
                                         final MongoProgress mongoProgress,
                                         final int actionsPerformed) {

        int completedSteps = 0;
        int actionsToApply = actionsPerformed;
        int remaining = mongoProgress.getRemaining();
        var step = mongoProgress.getCurrentStep();

        final var builder = new UpdateBuilder();
        final var mongoUser = mongoProgress.getProfile().getUser();

        while (step != null && actionsToApply >= remaining) {

            // We've hit the end of the mission the mission has no final repeat step and has no remaining
            // steps.  Therefore the mission is assumed to be complete.  No further rewards will be issued
            // and this effectively ignores the progress.

            // Assigns the rewards from the step

            final var _step = step;
            final var _completedSteps = completedSteps;
            final var rewardIssuances = step.getRewards()
                .stream()
                .filter(r -> r != null && r.getItem() != null)
                .map(r -> {

                    final var progress = getDozerMapper().map(mongoProgress, Progress.class);
                    final var __step = getDozerMapper().map(_step, Step.class);
                    final var reward = getDozerMapper().map(r, Reward.class);
                    final var user = getDozerMapper().map(mongoUser, User.class);
                    final var metadata = generateMissionProgressMetadata(progress, __step);

                    final int stepSequence = progress.getSequence() + _completedSteps;

                    final String context = buildMissionProgressContextString(
                        mongoProgress.getObjectId().toHexString(),
                        stepSequence,
                        __step.getRewards().indexOf(reward));

                    final var tags = buildRewardIssuanceTags(progress, stepSequence);

                    final var issuance = new RewardIssuance();
                    issuance.setItem(reward.getItem());
                    issuance.setItemQuantity(reward.getQuantity());
                    issuance.setUser(user);
                    issuance.setType(PERSISTENT);
                    issuance.setSource(MISSION_PROGRESS_SOURCE);
                    issuance.setMetadata(metadata);
                    issuance.setTags(tags);
                    issuance.setContext(context);

                    final var createdRewardIssuance = getRewardIssuanceDao().getOrCreateRewardIssuance(issuance);
                    return getDozerMapper().map(createdRewardIssuance, MongoRewardIssuance.class);

                }).collect(toList());

            builder.with(
                set("version", randomUUID().toString()),
                addToSet("rewardIssuances", rewardIssuances)
            );

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

        return builder.with(
            inc("sequence", completedSteps),
            set("version", randomUUID().toString()),
            set("remaining", step == null ? 0 : step.getCount() - actionsToApply)
        ).execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

    }

    public Map<String, Object> generateMissionProgressMetadata(Progress progress, Step step) {
        final Map<String, Object> map = new HashMap<>();
        final Map stepMap = getDozerMapper().map(step, Map.class);

        map.put(MISSION_PROGRESS_PROGRESS_KEY, progress.getId());
        map.put(MISSION_PROGRESS_STEP_KEY, stepMap);

        return map;
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
