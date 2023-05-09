package dev.getelements.elements.dao.mongo;

import com.mongodb.MongoCommandException;
import dev.getelements.elements.dao.ScoreDao;
import dev.getelements.elements.dao.mongo.model.MongoScore;
import dev.getelements.elements.dao.mongo.model.MongoScoreId;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.LeaderboardNotFoundException;
import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.experimental.filters.Filters;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.sql.Timestamp;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.getelements.elements.model.leaderboard.Leaderboard.TimeStrategyType.EPOCHAL;
import static dev.morphia.query.experimental.updates.UpdateOperators.*;
import static java.lang.System.currentTimeMillis;

public class MongoScoreDao implements ScoreDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MongoProfileDao mongoProfileDao;

    private MongoLeaderboardDao mongoLeaderboardDao;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public Score createOrUpdateScore(final String leaderboardNameOrId, final Score score) {

        getValidationHelper().validateModel(score, ValidationGroups.Create.class);

        final var mongoProfile = getMongoProfileDao().getActiveMongoProfile(score.getProfile());
        final var mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);
        final var leaderboardEpoch = mongoLeaderboard.getCurrentEpoch();

        // If the leaderboard is epochal, but the current time is less than the first epoch time...
        if (mongoLeaderboard.getTimeStrategyType() == EPOCHAL && !mongoLeaderboard.hasStarted()) {
            throw new LeaderboardNotFoundException("Leaderboard has not started its first epoch yet.");
        }

        final var mongoScoreId = new MongoScoreId(mongoProfile, mongoLeaderboard, leaderboardEpoch);

        final var query = getDatastore().find(MongoScore.class);
        query.filter(Filters.eq("_id", mongoScoreId));

        final var builder = new UpdateBuilder();

        // Set the timestamp to be "now" on create as well as update since an update essentially resets an existing
        // record
        final var now = new Timestamp(currentTimeMillis());

        builder.with(
            set("_id", mongoScoreId),
            set("profile", mongoProfile),
            set("leaderboard", mongoLeaderboard),
            set("leaderboardEpoch", leaderboardEpoch),
            set("creationTimestamp", now)
        );

        switch (mongoLeaderboard.getScoreStrategyType()) {
            case ACCUMULATE:
                builder.with(inc("pointValue", score.getPointValue()));
                break;
            case OVERWRITE_IF_GREATER:
                builder.with(max("pointValue", score.getPointValue()));
                break;
            default:
                throw new InternalException("Unexpected type: " + mongoLeaderboard.getScoreStrategyType());
        }

        try {

            final var result = builder.execute(query, new ModifyOptions()
                .upsert(true)
                .returnDocument(AFTER)
            );

            return getBeanMapper().map(result, Score.class);

        } catch (MongoCommandException ex) {

            // We only get a duplicate exception if the score is less than the provided score.  In which case we simply
            // return the existing score.  All other outcomes will either update or create the score.

            if (ex.getErrorCode() == 11000) {

                final var result = getDatastore()
                        .find(MongoScore.class)
                        .filter(Filters.eq("_id", mongoScoreId))
                    .first();

                return getBeanMapper().map(result, Score.class);

            } else {
                throw new InternalException(ex);
            }

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

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

    public MongoLeaderboardDao getMongoLeaderboardDao() {
        return mongoLeaderboardDao;
    }

    @Inject
    public void setMongoLeaderboardDao(MongoLeaderboardDao mongoLeaderboardDao) {
        this.mongoLeaderboardDao = mongoLeaderboardDao;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

}
