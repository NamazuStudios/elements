package dev.getelements.elements.dao.mongo.mission;

import com.mongodb.DuplicateKeyException;
import dev.getelements.elements.sdk.dao.MissionDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.model.mission.MongoMission;
import dev.getelements.elements.dao.mongo.model.mission.MongoReward;
import dev.getelements.elements.dao.mongo.model.mission.MongoStep;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.mission.MissionNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;
import static java.util.stream.Collectors.toList;

public class MongoMissionDao implements MissionDao {

    private static final Logger logger = LoggerFactory.getLogger(MongoMissionDao.class);

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MapperRegistry dozerMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private MongoItemDao mongoItemDao;

    private BooleanQueryParser booleanQueryParser;

    @Override
    public Pagination<Mission> getMissions(int offset, int count, List<String> tags)  {

        final Query<MongoMission> query = getDatastore()
                .find(MongoMission.class)
                .filter(exists("name"));

        if (tags != null && !tags.isEmpty()) {
            query.filter(in("tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(
                query,
                offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, Mission.class), new FindOptions()
        );

    }

    @Override
    public Pagination<Mission> getMissions(final int offset, final int count, final String search) {

        if (isNullOrEmpty(search)) {
            return getMissions(offset, count, List.of());
        }

        final var query = getBooleanQueryParser()
                .parse(MongoMission.class, search)
                .orElseGet(() -> getDatastore().find(MongoMission.class).filter(text(search)))
                .filter(exists("name"));

        return getMongoDBUtils().isScanQuery(query)
            ? Pagination.empty()
            : getMongoDBUtils().paginationFromQuery(
                query,
                offset, count,
                mongoItem -> getDozerMapper().map(mongoItem, Mission.class), new FindOptions()
        );

    }

    @Override
    public Optional<Mission> findMissionByNameOrId(final String missionNameOrId) {
        return findMongoMissionByNameOrId(missionNameOrId).map(mm -> getDozerMapper().map(mm, Mission.class));
    }

    public Optional<MongoMission> findMongoMissionByNameOrId(final String missionNameOrId) {
        final var query = getQueryForNameOrId(missionNameOrId);
        final MongoMission mission = query.first();
        return Optional.ofNullable(mission);
    }

    public Query<MongoMission> getQueryForNameOrId(final String missionNameOrId) {
        return getMongoDBUtils()
                .parse(missionNameOrId)
                .map(oid -> getDatastore().find(MongoMission.class).filter(eq("_id", oid)))
                .orElseGet(() -> getDatastore().find(MongoMission.class).filter(eq("name", missionNameOrId)))
                .filter(exists("name"));
    }

    @Override
    public Mission getMissionByNameOrId(final String identifier) {
        final MongoMission mongoMission = getMongoMissionByNameOrId(identifier);
        return getDozerMapper().map(mongoMission, Mission.class);
    }

    public MongoMission getMongoMissionByNameOrId(final String missionNameOrId) {
        return findMongoMissionByNameOrId(missionNameOrId).orElseThrow(MissionNotFoundException::new);
    }

    @Override
    public List<Mission> getMissionsMatching(final Collection<String> missionNamesOrIds) {

        if (missionNamesOrIds.isEmpty()) {
            return List.of();
        }

        final var objectIds = missionNamesOrIds
                .stream()
                .map(id -> getMongoDBUtils().parse(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        final var names = missionNamesOrIds
                .stream()
                .filter(id -> getMongoDBUtils().parse(id).isEmpty())
                .collect(toList());

        final var query = getDatastore().find(MongoMission.class)
                .filter(or(
                        in("_id", objectIds),
                        in("name", names)
                ));

        try (final var stream = query.stream()) {
            return stream
                    .map(m -> getDozerMapper().map(m, Mission.class))
                    .collect(toList());
        }

    }

    @Override
    public Mission updateMission(final Mission mission) {

        getValidationHelper().validateModel(mission, Update.class);
        normalize(mission);

        if ((mission.getSteps() == null || mission.getSteps().size() == 0) && mission.getFinalRepeatStep() == null) {
            throw new InvalidDataException("At least one of Steps or finalRepeatStep must be provided.");
        }

        final var mongoMission = checkMission(mission);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(mission.getId());

        final var query = getDatastore()
            .find(MongoMission.class)
            .filter(eq("_id", objectId));

        final var builder = new UpdateBuilder();

        builder.with(
            set("name", mongoMission.getName()),
            set("displayName", mongoMission.getDisplayName()),
            set("description", mongoMission.getDescription())
        );

        mission.validateTags();

        if (mission.getTags() != null) {
            builder.with(set("tags", mongoMission.getTags()));
        } else {
            builder.with(unset("tags"));
        }

        if (mission.getSteps() != null) {
            builder.with(set("steps", mongoMission.getSteps()));
        } else {
            builder.with(unset("steps"));
        }

        if (mission.getFinalRepeatStep() != null) {
            builder.with(set("finalRepeatStep", mongoMission.getFinalRepeatStep()));
        } else {
            builder.with(unset("finalRepeatStep"));
        }

        if (mission.getMetadata() != null) {
            builder.with(set("metadata", mongoMission.getMetadata()));
        } else {
            builder.with(unset("metadata"));
        }

        final var updatedMongoItem = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (updatedMongoItem == null) {
            throw new MissionNotFoundException("Mission with id or name of " + mission.getId() + " does not exist");
        }

        return getDozerMapper().map(updatedMongoItem, Mission.class);

    }

    @Override
    public Mission createMission(final Mission mission) {

        getValidationHelper().validateModel(mission, Insert.class);

        if ((mission.getSteps() == null || mission.getSteps().size() == 0) && mission.getFinalRepeatStep() == null) {
            throw new InvalidDataException("At least one of Steps or finalRepeatStep must be provided.");
        }

        normalize(mission);

        final MongoMission mongoMission = checkMission(mission);

        try {
            getDatastore().insert(mongoMission);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        final Query<MongoMission> query = getDatastore().find(MongoMission.class);
        query.filter(eq("_id", mongoMission.getObjectId()));

        return getDozerMapper().map(query.first(), Mission.class);

    }

    private MongoMission checkMission(final Mission mission) {
        final MongoMission mongoMission = getDozerMapper().map(mission, MongoMission.class);
        return checkMission(mongoMission);
    }

    private MongoMission checkMission(final MongoMission mongoMission) {

        if (mongoMission.getSteps() != null) {
            final List<MongoStep> mongoSteps = checkSteps(mongoMission.getSteps());
            mongoMission.setSteps(mongoSteps);
        }

        if (mongoMission.getFinalRepeatStep() != null) {
            final MongoStep mongoStep = checkStep(mongoMission.getFinalRepeatStep());
            mongoMission.setFinalRepeatStep(mongoStep);
        }

        return mongoMission;

    }

    private List<MongoStep> checkSteps(final List<MongoStep> mongoSteps) {
        return mongoSteps.stream().map(mongoStep -> checkStep(mongoStep)).collect(toList());
    }

    private MongoStep checkStep(final MongoStep mongoStep) {

        if (mongoStep.getRewards() == null) return null;

        final List<MongoReward> mongoRewards = mongoStep.getRewards()
            .stream()
            .filter(mongoReward -> mongoReward != null)
            .map(mongoReward -> checkReward(mongoReward))
            .collect(toList());

        mongoStep.setRewards(mongoRewards);
        return mongoStep;

    }

    private MongoReward checkReward(final MongoReward mongoReward) {
        final MongoItem mongoItem = mongoReward.getItem();
        final MongoItem refreshedMongoItem = getMongoItemDao().refresh(mongoItem);
        mongoReward.setItem(refreshedMongoItem);

        return mongoReward;
    }

    @Override
    public void deleteMission(final String missionNameOrID) {

        final var modifiedCount = getQueryForNameOrId(missionNameOrID)
                .update(new UpdateOptions(), unset("name"))
                .getModifiedCount();

        if (modifiedCount == 0) {
            throw new NotFoundException("Mission not found: " + missionNameOrID);
        }

    }

    private void normalize(Mission item) {
        item.validateTags();
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
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

    public MongoItemDao getMongoItemDao() {
        return mongoItemDao;
    }

    @Inject
    public void setMongoItemDao(MongoItemDao mongoItemDao) {
        this.mongoItemDao = mongoItemDao;
    }

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

}
