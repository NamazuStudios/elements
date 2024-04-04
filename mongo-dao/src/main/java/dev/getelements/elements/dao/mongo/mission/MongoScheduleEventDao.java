package dev.getelements.elements.dao.mongo.mission;

import dev.getelements.elements.dao.ScheduleEventDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.mission.MongoScheduleEvent;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.exception.mission.ScheduleEventNotFoundException;
import dev.getelements.elements.exception.mission.ScheduleNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Read;
import dev.getelements.elements.model.mission.ScheduleEvent;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoScheduleEventDao implements ScheduleEventDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private Mapper mapper;

    private BooleanQueryParser booleanQueryParser;

    private MongoMissionDao mongoMissionDao;

    private MongoScheduleDao mongoScheduleDao;

    @Override
    public ScheduleEvent createScheduleEvent(final ScheduleEvent scheduleEvent) {

        getValidationHelper().validateModel(scheduleEvent, Insert.class);
        getValidationHelper().validateModel(scheduleEvent.getSchedule(), Read.class);
        scheduleEvent.getMissions().forEach(m -> getValidationHelper().validateModel(m, Read.class));

        final var mongoSchedule = getMongoScheduleDao()
                .findMongoScheduleByNameOrId(scheduleEvent.getSchedule().getId())
                .orElseThrow(ScheduleNotFoundException::new);

        final var mongoMissions = scheduleEvent
                .getMissions()
                .stream()
                .map(mission -> getMongoMissionDao().getMongoMissionByNameOrId(mission.getId()))
                .collect(Collectors.toList());

        final var mongoScheduleEvent = getMapper().map(scheduleEvent, MongoScheduleEvent.class);
        mongoScheduleEvent.setSchedule(mongoSchedule);
        mongoScheduleEvent.setMissions(mongoMissions);

        return getMongoDBUtils().perform(ds -> ds.save(mongoScheduleEvent), ScheduleEvent.class);

    }

    @Override
    public ScheduleEvent updateScheduleEvent(final ScheduleEvent scheduleEvent) {

        getValidationHelper().validateModel(scheduleEvent, Insert.class);
        getValidationHelper().validateModel(scheduleEvent.getSchedule(), Read.class);
        scheduleEvent.getMissions().forEach(m -> getValidationHelper().validateModel(m, Read.class));

        final var mongoSchedule = getMongoScheduleDao()
                .findMongoScheduleByNameOrId(scheduleEvent.getSchedule().getId())
                .orElseThrow(ScheduleNotFoundException::new);

        final var mongoMissions = scheduleEvent
                .getMissions()
                .stream()
                .map(mission -> getMongoMissionDao().getMongoMissionByNameOrId(mission.getId()))
                .collect(Collectors.toList());

        final var objectId = getMongoDBUtils().parseOrThrow(scheduleEvent.getId(), ScheduleNotFoundException::new);

        final var query = getDatastore().find(MongoScheduleEvent.class)
                .filter(eq("_id", objectId))
                .filter(eq("schedule", mongoSchedule));

        final var builder = new UpdateBuilder()
                .with(set("schedule", mongoSchedule))
                .with(set("missions", mongoMissions))
                .with(scheduleEvent.getEnd() == null ? unset("end") : set("end", scheduleEvent.getEnd()))
                .with(scheduleEvent.getBegin() == null ? unset("begin") : set("begin", scheduleEvent.getBegin()));

        return getMongoDBUtils().perform(ds -> builder.update(query), ScheduleEvent.class);

    }

    @Override
    public Pagination<ScheduleEvent> getScheduleEvents(final String scheduleNameOrId,
                                                       final int offset, final int count) {

        final var mongoSchedule = getMongoScheduleDao()
                .findMongoScheduleByNameOrId(scheduleNameOrId)
                .orElseThrow(ScheduleNotFoundException::new);

        final var query = getDatastore().find(MongoScheduleEvent.class)
                .filter(eq("schedule", mongoSchedule));

        return getMongoDBUtils().paginationFromQuery(
                query,
                offset, count,
                se -> getDozerMapper().map(se, ScheduleEvent.class));

    }

    @Override
    public Pagination<ScheduleEvent> getScheduleEvents(final String scheduleNameOrId,
                                                       final int offset, final int count,
                                                       final String search) {

        if (isNullOrEmpty(search)) {
            return getScheduleEvents(scheduleNameOrId, offset, count);
        }

        final var mongoSchedule = getMongoScheduleDao()
                .findMongoScheduleByNameOrId(scheduleNameOrId)
                .orElseThrow(ScheduleNotFoundException::new);

        final var query = getBooleanQueryParser()
                .parse(MongoScheduleEvent.class, search)
                .map(q -> q.filter(eq("schedule", mongoSchedule)));

        return query.isEmpty() || getMongoDBUtils().isScanQuery(query.get())
                ? Pagination.empty()
                : getMongoDBUtils().paginationFromQuery(
                        query.get(),
                        offset, count,
                        se -> getDozerMapper().map(se, ScheduleEvent.class)
                );

    }

    @Override
    public Optional<ScheduleEvent> findScheduleEventByNameOrId(
            final String scheduleNameOrId,
            final String scheduleEventId) {
        return findMongoScheduleEventByNameOrId(scheduleNameOrId, scheduleEventId)
                .map(se -> getDozerMapper().map(se, ScheduleEvent.class));
    }

    public Optional<MongoScheduleEvent> findMongoScheduleEventByNameOrId(
            final String scheduleNameOrId,
            final String scheduleEventId) {
        final var query = getQuery(scheduleNameOrId, scheduleEventId);
        return Optional.ofNullable(query.first());
    }

    public Query<MongoScheduleEvent> getQuery(final String scheduleNameOrId,
                                              final String scheduleEventId) {

        final var objectId = getMongoDBUtils().parse(scheduleEventId)
                .orElseThrow(ScheduleEventNotFoundException::new);

        final var mongoSchedule = getMongoScheduleDao()
                .findMongoScheduleByNameOrId(scheduleNameOrId)
                .orElseThrow(ScheduleNotFoundException::new);

        return getDatastore()
                .find(MongoScheduleEvent.class)
                .filter(eq("_id", objectId))
                .filter(eq("schedule", mongoSchedule));

    }

    @Override
    public void deleteScheduleEvents(final String scheduleNameOrId) {

        final var mongoSchedule = getMongoScheduleDao()
                .findMongoScheduleByNameOrId(scheduleNameOrId)
                .orElseThrow(ScheduleNotFoundException::new);

        final var query = getDatastore().find(MongoScheduleEvent.class)
                .filter(eq("schedule", mongoSchedule));

        query.delete();

    }

    @Override
    public void deleteScheduleEvent(final String scheduleNameOrId,
                                    final String scheduleEventId) {

        final var result = getQuery(scheduleNameOrId, scheduleEventId).delete();

        if (result.getDeletedCount() == 0) {
            throw new ScheduleEventNotFoundException();
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

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
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

}
