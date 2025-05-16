package dev.getelements.elements.dao.mongo.mission;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.sdk.dao.ProgressDao;
import dev.getelements.elements.dao.mongo.*;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils.ContentionException;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.mission.*;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.TooBusyException;
import dev.getelements.elements.sdk.model.exception.mission.ProgressNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.Tabulation;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.ProgressRow;
import dev.getelements.elements.sdk.model.mission.Step;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.reward.Reward;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.Query;

import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.dao.mongo.model.mission.MongoProgressId.parseOrThrowNotFoundException;
import static dev.getelements.elements.sdk.model.mission.Step.buildRewardIssuanceTags;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.*;
import static dev.getelements.elements.sdk.model.reward.RewardIssuance.Type.PERSISTENT;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.in;
import static dev.morphia.query.updates.UpdateOperators.*;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoProgressDao implements ProgressDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoProgressDao.class);

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private ObjectMapper objectMapper;

    private MapperRegistry mapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private MongoMissionDao mongoMissionDao;

    private MongoProfileDao mongoProfileDao;

    private MongoRewardIssuanceDao rewardIssuanceDao;

    private BooleanQueryParser booleanQueryParser;

    @Override
    public Pagination<Progress> getProgresses(final Profile profile,
                                              final int offset, final int count,
                                              final List<String> tags)  {

        final var mongoProfileOptional = getMongoProfileDao()
                .findActiveMongoProfile(profile);

        if (mongoProfileOptional.isEmpty()) {
            return Pagination.empty();
        }

        final var query = getDatastore()
                .find(MongoProgress.class)
                .filter(eq("profile", mongoProfileOptional.get()));

        if (tags != null && !tags.isEmpty()) {
            query.filter(in("missionTags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoItem -> getMapperRegistry().map(mongoItem, Progress.class));

    }

    @Override
    public Pagination<Progress> getProgresses(final Profile profile,
                                              final int offset, final int count,
                                              final List<String> tags,
                                              final String search) {

        final var mongoProfileOptional = getMongoProfileDao()
                .findActiveMongoProfile(profile);

        if (mongoProfileOptional.isEmpty()) {
            return Pagination.empty();
        }

        final var query = getBooleanQueryParser()
                .parse(getDatastore().find(MongoProgress.class), search)
                .filter(q -> getMongoDBUtils().isIndexedQuery(q))
                .orElseGet(() -> getDatastore().find(MongoProgress.class));

        query.filter(eq("profile",mongoProfileOptional.get()));

        if (tags != null && !tags.isEmpty()) {
            query.filter(in("missionTags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoItem -> getMapperRegistry().map(mongoItem, Progress.class));

    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count, final List<String> tags)  {

        final var query = getDatastore().find(MongoProgress.class);

        if (tags != null && !tags.isEmpty()) {
            query.filter(in("missionTags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoItem -> getMapperRegistry().map(mongoItem, Progress.class));

    }

    @Override
    public Pagination<Progress> getProgresses(final int offset, final int count,
                                              final List<String> tags, final String search) {

        final var query = getBooleanQueryParser()
                .parse(getDatastore().find(MongoProgress.class), search)
                .filter(q -> getMongoDBUtils().isIndexedQuery(q))
                .orElseGet(() -> getDatastore().find(MongoProgress.class));

        if (tags != null && !tags.isEmpty()) {
            query.filter(in("missionTags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                mongoItem -> getMapperRegistry().map(mongoItem, Progress.class));

    }

    @Override
    public Tabulation<ProgressRow> getProgressesTabular() {
        final var query = getDatastore().find(MongoProgress.class);
        return getMongoDBUtils().tabulationFromQuery(query, mp -> getMapperRegistry().map(mp, ProgressRow.class));
    }

    @Override
    public Optional<Progress> findProgress(final String identifier) {

        if (isEmpty(nullToEmpty(identifier).trim())) {
            throw new ProgressNotFoundException("Unable to find progress with an id " + identifier);
        }

        final MongoProgressId mongoProgressId = parseOrThrowNotFoundException(identifier);
        final Query<MongoProgress> query = getDatastore().find(MongoProgress.class);

        query.filter(eq("_id", mongoProgressId));

        final MongoProgress mongoProgress = query.first();
        return Optional.ofNullable(mongoProgress).map(p -> getMapperRegistry().map(p, Progress.class));

    }

    @Override
    public Optional<Progress> findProgressForProfileAndMission(final Profile profile, final String missionNameOrId) {

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);
        final MongoMission mongoMission = getMongoMissionDao().getMongoMissionByNameOrId(missionNameOrId);

        final var mongoProgressId = new MongoProgressId(mongoProfile, mongoMission);
        final Query<MongoProgress> query = getDatastore()
                .find(MongoProgress.class)
                .filter(eq("_id", mongoProgressId));

        final MongoProgress mongoProgress = query.first();
        return Optional.ofNullable(mongoProgress).map(p -> getMapperRegistry().map(p, Progress.class));

    }

    @Override
    public Progress updateProgress(final Progress progress) {

        getValidationHelper().validateModel(progress, Update.class);

        final var mongoProgressId = parseOrThrowNotFoundException(progress.getId());

        final var query = getDatastore().find(MongoProgress.class);
        query.filter(eq("_id", mongoProgressId));

        final var mongoProgress = new UpdateBuilder().with(
                set("version", randomUUID().toString()),
                set("remaining", progress.getRemaining()),
                set("currentStep", progress.getCurrentStep())
            ).execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));

        if (mongoProgress == null) {
            throw new ProgressNotFoundException("Progress with id or name of " + progress.getId() + " does not exist");
        }

        return getMapperRegistry().map(mongoProgress, Progress.class);

    }

    @Override
    public Progress createOrGetExistingProgress(final Progress progress) {

        getValidationHelper().validateModel(progress, Insert.class);

        final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(progress.getProfile());
        final var mongoMission = getMongoMissionDao().getMongoMissionByNameOrId(progress.getMission().getId());
        final var mongoProgressId = new MongoProgressId(mongoProfile, mongoMission);

        final var query = getDatastore()
                .find(MongoProgress.class)
                .filter(eq("_id", mongoProgressId));

        final var steps = mongoMission.getSteps();
        final var finalRepeatStep = mongoMission.getFinalRepeatStep();

        final MongoStep first;

        if (steps == null || steps.isEmpty()) {
            if (finalRepeatStep == null) throw new InvalidDataException("one step must be defined");
            first = finalRepeatStep;
        } else {
            first = steps.get(0);
        }

        final var missionTags = mongoMission.getTags();

        final var result = new UpdateBuilder().with(
                set("_id", mongoProgressId),
                set("profile", mongoProfile),
                set("mission", mongoMission),
                missionTags == null
                        ? unset("missionTags")
                        : set("missionTags", missionTags),
                setOnInsert(Map.of(
                        "version", randomUUID().toString(),
                        "remaining", first.getCount(),
                        "rewardIssuances", List.of(),
                        "managedBySchedule", false,
                        "schedules", List.of(),
                        "scheduleEvents", List.of()
                ))
        ).execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER));

        return getMapperRegistry().map(result, Progress.class);

    }

    @Override
    public void deleteProgress(final String progressId) {

        final MongoProgressId mongoProgressId = parseOrThrowNotFoundException(progressId);
        final Query<MongoProgress> query = getDatastore().find(MongoProgress.class);
        query.filter(eq("_id", mongoProgressId));
        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new ProgressNotFoundException("Progress not found: " + progressId);
        }
    }

    @Override
    public Progress advanceProgress(final Progress progress, final int actionsPerformed) {

        final MongoProgress mongoProgress;

        try {
            mongoProgress = getMongoConcurrentUtils().performOptimistic(ads -> doAdvanceProgress(progress, actionsPerformed));
        } catch (MongoConcurrentUtils.ConflictException e) {
            throw new TooBusyException(e);
        }

        return getMapperRegistry().map(mongoProgress, Progress.class);

    }

    private MongoProgress doAdvanceProgress(final Progress progress, final int actionsPerformed) throws ContentionException {

        final var mongoProgressId = parseOrThrowNotFoundException(progress.getId());

        final var query = getDatastore().find(MongoProgress.class);
        query.filter(eq("_id", mongoProgressId));

        final var mongoProgress = query.first();

        if (mongoProgress == null)
            throw new ProgressNotFoundException("Progress with id not found: " + progress.getId());

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
        return new UpdateBuilder().with(
                set("version", randomUUID().toString()),
                dec("remaining", actionsPerformed)
        ).execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER));
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

                    final var progress = getMapperRegistry().map(mongoProgress, Progress.class);
                    final var __step = getMapperRegistry().map(_step, Step.class);
                    final var reward = getMapperRegistry().map(r, Reward.class);
                    final var user = getMapperRegistry().map(mongoUser, User.class);
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
                    return getMapperRegistry().map(createdRewardIssuance, MongoRewardIssuance.class);

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
        final Map<String, Object> stepMap = getObjectMapper()
                .convertValue(
                        step,
                        new TypeReference<>() {}
                );

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

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry dozerMapperRegistry) {
        this.mapperRegistry = dozerMapperRegistry;
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

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
