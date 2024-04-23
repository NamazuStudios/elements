package dev.getelements.elements.dao.mongo.mission;

import dev.getelements.elements.dao.ScheduleProgressDao;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.mission.*;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups.Read;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.mission.ScheduleEvent;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.elemMatch;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.*;
import static dev.morphia.query.updates.UpdateOperators.addToSet;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class MongoScheduleProgressDao implements ScheduleProgressDao {


    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private Mapper mapper;

    private MongoProfileDao mongoProfileDao;

    private MongoMissionDao mongoMissionDao;

    private MongoScheduleDao mongoScheduleDao;

    private MongoScheduleEventDao mongoScheduleEventDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    @Override
    public Pagination<Progress> getProgresses(
            final String profileId,
            final String scheduleNameOrId,
            final int offset, final int count) {
        return getMongoScheduleDao()
                .findMongoScheduleByNameOrId(scheduleNameOrId)
                .map(mongoSchedule -> {
                    final var query = getDatastore().find(MongoProgress.class);
                    query.filter(elemMatch("schedules", eq("$ref", mongoSchedule.getObjectId())));
                    return getMongoDBUtils().paginationFromQuery(
                            query,
                            offset, count,
                            p -> getDozerMapper().map(p, Progress.class));
                })
                .orElseGet(Pagination::empty);
    }

    @Override
    public List<Progress> createProgressesForMissionsIn(
            final String scheduleNameOrId,
            final String profileId,
            final List<ScheduleEvent> events) {

        final var mongoProfileOptional = getMongoProfileDao().findActiveMongoProfile(profileId);

        if (mongoProfileOptional.isEmpty()) {
            return List.of();
        }

        final var mongoProfile = mongoProfileOptional.get();

        return events.stream()
                .map(ev -> getValidationHelper().validateModel(ev, Read.class))
                .map(ev -> getMongoScheduleEventDao().findMongoScheduleEventById(scheduleNameOrId, ev.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(mongoEvent -> mongoEvent
                        .getMissions()
                        .stream()
                        .map(mongoMission -> doCreateProgress(mongoProfile, mongoEvent, mongoMission)))
                .map(mp -> getDozerMapper().map(mp, Progress.class))
                .collect(toList());

    }

    private MongoProgress doCreateProgress(final MongoProfile mongoProfile,
                                           final MongoScheduleEvent mongoScheduleEvent,
                                           final MongoMission mongoMission) {

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

        return new UpdateBuilder().with(
                set("_id", mongoProgressId),
                set("profile", mongoProfile),
                set("mission", mongoMission),
                setOnInsert(Map.of(
                        "version", randomUUID().toString(),
                        "remaining", first.getCount(),
                        "rewardIssuances", List.of(),
                        "managedBySchedule", true,
                        "missionTags", mongoMission.getTags()
                )),
                addToSet("schedules", mongoScheduleEvent.getSchedule()),
                addToSet("scheduleEvents", mongoScheduleEvent)
        ).execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER));

    }

    @Override
    public long deleteProgressesForMissionsNotIn(
            final String scheduleNameOrId,
            final String profileId,
            final List<ScheduleEvent> events) {

        final var mongoProfileOptional = getMongoProfileDao().findActiveMongoProfile(profileId);
        final var mongoScheduleOptional = getMongoScheduleDao().findMongoScheduleByNameOrId(scheduleNameOrId);

        if (mongoProfileOptional.isEmpty() || mongoScheduleOptional.isEmpty()) {
            return 0;
        }

        final var mongoProfile = mongoProfileOptional.get();
        final var mongoSchedule = mongoScheduleOptional.get();

        final var mongoScheduleEvents = events.stream()
                .map(ev -> getValidationHelper().validateModel(ev, Read.class))
                .map(ev -> getMongoScheduleEventDao().findMongoScheduleEventById(scheduleNameOrId, ev.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        final var mongoMissionIds = mongoScheduleEvents
                .stream()
                .flatMap(ev -> ev.getMissions().stream())
                .map(MongoMission::getObjectId)
                .collect(toSet());

        final var progressStream = getDatastore()
                .find(MongoProgress.class)
                .filter(eq("_id.profileId", mongoProfile.getObjectId()))
                .stream();

        try (progressStream) {
            return progressStream.mapToLong(mongoProgress -> {

                    if (mongoProgress == null) {
                        return 0;
                    }

                    final var removedEvent = mongoProgress
                            .getScheduleEvents()
                            .removeIf(mongoScheduleEvent -> mongoScheduleEvent
                                    .getMissions()
                                    .stream()
                                    .noneMatch(mongoMission -> mongoMissionIds.contains(mongoMission.getObjectId()))
                            );

                    final var removeSchedule = removedEvent && mongoProgress
                            .getScheduleEvents()
                            .stream()
                            .map(MongoScheduleEvent::getSchedule)
                            .filter(Objects::nonNull)
                            .noneMatch(sc -> Objects.equals(sc.getObjectId(), mongoSchedule.getObjectId()));

                    final var removedSchedule = removeSchedule && mongoProgress
                            .getSchedules()
                            .removeIf(sc -> Objects.equals(sc.getObjectId(), mongoSchedule.getObjectId()));

                    if (mongoProgress.isManagedBySchedule() && mongoProgress.getScheduleEvents().isEmpty()) {
                        getDatastore().delete(mongoProgress);
                    } else {
                        getDatastore().save(mongoProgress);
                    }

                    return removedSchedule ? 1 : 0;
            })
            .reduce(0, Long::sum);
        }

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

    public MongoMissionDao getMongoMissionDao() {
        return mongoMissionDao;
    }

    @Inject
    public void setMongoMissionDao(MongoMissionDao mongoMissionDao) {
        this.mongoMissionDao = mongoMissionDao;
    }

    public MongoScheduleDao getMongoScheduleDao() {
        return mongoScheduleDao;
    }

    @Inject
    public void setMongoScheduleDao(MongoScheduleDao mongoScheduleDao) {
        this.mongoScheduleDao = mongoScheduleDao;
    }

    public MongoScheduleEventDao getMongoScheduleEventDao() {
        return mongoScheduleEventDao;
    }

    @Inject
    public void setMongoScheduleEventDao(MongoScheduleEventDao mongoScheduleEventDao) {
        this.mongoScheduleEventDao = mongoScheduleEventDao;
    }

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

}
