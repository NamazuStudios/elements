package dev.getelements.elements.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoCommandException;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.sdk.dao.LeaderboardDao;
import dev.getelements.elements.dao.mongo.model.MongoLeaderboard;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.exception.*;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.util.regex.Pattern;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.exists;
import static dev.morphia.query.updates.UpdateOperators.set;

public class MongoLeaderboardDao implements LeaderboardDao {

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapperRegistry;

    @Override
    public Leaderboard createLeaderboard(final Leaderboard leaderboard) {

        getValidationHelper().validateModel(leaderboard, ValidationGroups.Create.class);

        switch (leaderboard.getTimeStrategyType()) {
            case ALL_TIME:
                if (leaderboard.getFirstEpochTimestamp() != null || leaderboard.getEpochInterval() != null) {
                    throw new InvalidDataException("firstEpochTimestamp and epochInterval should not be provided " +
                            "for an ALL_TIME time strategy.");
                }
                break;
            case EPOCHAL:
                if (leaderboard.getFirstEpochTimestamp() == null || leaderboard.getEpochInterval() == null) {
                    throw new InvalidDataException("firstEpochTimestamp and epochInterval must both be provided " +
                            "for an EPOCHAL time strategy.");
                }
                break;
            default:
                break;
        }

        try {
            final Leaderboard existingLeaderboard = getLeaderboard(leaderboard.getName());
            if (existingLeaderboard != null) {
                throw new DuplicateException("Leaderboard with the given name already exists");
            }
        }
        catch (NotFoundException e) {

        }

        final MongoLeaderboard mongoLeaderboard = getBeanMapper().map(leaderboard, MongoLeaderboard.class);

        try {
            getDatastore().insert(mongoLeaderboard);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        return getBeanMapper().map(mongoLeaderboard, Leaderboard.class);

    }

    @Override
    public Pagination<Leaderboard> getLeaderboards(final int offset, final int count) {
        final Query<MongoLeaderboard> query = getDatastore().find(MongoLeaderboard.class);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, l -> getBeanMapper().map(l, Leaderboard.class), new FindOptions());
    }

    @Override
    public Pagination<Leaderboard> getLeaderboards(final int offset, final int count, final String search) {

        final Query<MongoLeaderboard> mongoQuery = getDatastore().find(MongoLeaderboard.class).filter(exists("name"));

        if (StringUtils.isNotEmpty(search)) {
            mongoQuery.filter(
                    Filters.regex("name", Pattern.compile(search))
            );
        }

        return getMongoDBUtils().paginationFromQuery(
                mongoQuery,
                offset, count,
                mongoLeaderboard -> getBeanMapper().map(mongoLeaderboard, Leaderboard.class),
                new FindOptions()
        );

    }

    @Override
    public Leaderboard getLeaderboard(final String nameOrId) {
        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboard(nameOrId);
        return getBeanMapper().map(mongoLeaderboard, Leaderboard.class);

    }

    public MongoLeaderboard getMongoLeaderboard(final String nameOrId) {

        final Query<MongoLeaderboard> query = datastore.find(MongoLeaderboard.class);

        try {
            query.filter(Filters.eq("_id", new ObjectId(nameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("name", nameOrId));
        }

        final MongoLeaderboard mongoLeaderboard = query.first();

        if (mongoLeaderboard == null) {
            throw new LeaderboardNotFoundException("Leaderboard " + nameOrId + " not found.");
        }

        return mongoLeaderboard;

    }

    @Override
    public Leaderboard updateLeaderboard(final String leaderboardNameOrId, final Leaderboard leaderboard) {

        if (leaderboardNameOrId == null) {
            throw new IllegalArgumentException("Leaderboard name must not be null");
        }

        getValidationHelper().validateModel(leaderboard, ValidationGroups.Update.class);

        final var query = datastore.find(MongoLeaderboard.class);

        try {
            query.filter(Filters.eq("_id", new ObjectId(leaderboardNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("_id", leaderboardNameOrId));
        }

        // for now, do not allow updating of firstEpochTimestamp or epochInterval

        final var mongoLeaderboard = getMongoDBUtils().perform(ds->
            query.modify(
                set("name", leaderboard.getName()),
                set("title", leaderboard.getTitle()),
                set("scoreUnits", leaderboard.getScoreUnits())
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoLeaderboard == null) {
            throw new LeaderboardNotFoundException("Leaderboard not found: " + leaderboardNameOrId);
        }

        return getBeanMapper().map(mongoLeaderboard, Leaderboard.class);

    }

    @Override
    public void deleteLeaderboard(final String leaderboardNameOrId) {

        final Query<MongoLeaderboard> query = datastore.find(MongoLeaderboard.class);

        query.filter(Filters.eq("active", true));

        try {
            query.filter(Filters.eq("_id", new ObjectId(leaderboardNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("name", leaderboardNameOrId));
        }

        final DeleteResult deleteResult = query.delete();

        if(deleteResult.getDeletedCount() == 0){
            throw new LeaderboardNotFoundException();
        }

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

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getBeanMapper() {
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
    }

}
