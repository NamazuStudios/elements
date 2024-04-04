package dev.getelements.elements.dao.mongo.mission;

import dev.getelements.elements.dao.ScheduleDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.mission.MongoSchedule;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.exception.mission.ScheduleNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.updates.UpdateOperators;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.Optional;

import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.text;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoScheduleDao implements ScheduleDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private Mapper mapper;

    private BooleanQueryParser booleanQueryParser;

    @Override
    public Schedule create(final Schedule schedule) {
        getValidationHelper().validateModel(schedule, Insert.class);
        final var mongoSchedule = getMapper().map(schedule, MongoSchedule.class);
        final var asWritten = getMongoDBUtils().perform(ds -> ds.save(mongoSchedule), Schedule.class);
        return asWritten;
    }

    @Override
    public Optional<Schedule> findSchedulByNameOrId(final String scheduleNameOrId) {
        return findMongoScheduleByNameOrId(scheduleNameOrId).map(ms -> getMapper().map(ms, Schedule.class));
    }

    public Optional<MongoSchedule> findMongoScheduleByNameOrId(final String scheduleNameOrId) {
        final var query = getScheduleQuery(scheduleNameOrId);
        return Optional.ofNullable(query.first());
    }

    public Query<MongoSchedule> getScheduleQuery(final String scheduleNameOrId) {
        return getMongoDBUtils()
                .parse(scheduleNameOrId)
                .map(objectId -> getDatastore()
                        .find(MongoSchedule.class)
                        .filter(eq("_id", objectId))
                ).orElseGet(() -> getDatastore()
                        .find(MongoSchedule.class)
                        .filter(eq("name", scheduleNameOrId))
                );
    }

    @Override
    public Pagination<Schedule> getSchedules(final int offset, final int count) {

        final var query = getDatastore().find(MongoSchedule.class);

        return getMongoDBUtils().paginationFromQuery(
                query,
                offset, count,
                ms -> getDozerMapper().map(ms, Schedule.class)
        );

    }

    @Override
    public Pagination<Schedule> getSchedules(final int offset, final int count, final String search) {

        final var query = getBooleanQueryParser()
                .parse(MongoSchedule.class, search)
                .orElseGet(() -> getDatastore().find(MongoSchedule.class).filter(text(search)));

        return getMongoDBUtils().isScanQuery(query)
                ? Pagination.empty()
                : getMongoDBUtils().paginationFromQuery(
                        query,
                        offset, count,
                        ms -> getDozerMapper().map(ms, Schedule.class)
                );

    }

    @Override
    public Schedule updateSchedule(final Schedule updatedSchedule) {

        getValidationHelper().validateModel(updatedSchedule, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(updatedSchedule.getId(), ScheduleNotFoundException::new);

        final var query = getDatastore()
                .find(MongoSchedule.class)
                .filter(eq("_id", objectId));

        final var result = getMongoDBUtils().perform(
                ds -> new UpdateBuilder()
                        .with(set("name", updatedSchedule.getName()))
                        .with(set("displayName", updatedSchedule.getDisplayName()))
                        .with(set("description", updatedSchedule.getDescription()))
                        .modify(query)
                        .execute(),
                Schedule.class
        );

        if (result == null) {
            throw new ScheduleNotFoundException();
        }

        return result;

    }

    @Override
    public void deleteSchedule(final String scheduleNameOrId) {

        final var query = getScheduleQuery(scheduleNameOrId);
        final var result = new UpdateBuilder()
                .with(UpdateOperators.unset("name"))
                .update(query)
                .execute();

        if (result.getMatchedCount() == 0) {
            throw new ScheduleNotFoundException();
        }

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
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

}
