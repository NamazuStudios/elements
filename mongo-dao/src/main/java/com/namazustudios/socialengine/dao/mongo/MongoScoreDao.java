package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.namazustudios.socialengine.dao.ScoreDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoLeaderboard;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoScore;
import com.namazustudios.socialengine.dao.mongo.model.MongoScoreId;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.leaderboard.Score;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.dozer.Mapper;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.sql.Timestamp;

import static java.lang.System.currentTimeMillis;

public class MongoScoreDao implements ScoreDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MongoProfileDao mongoProfileDao;

    private MongoLeaderboardDao mongoLeaderboardDao;

    private Mapper beanMapper;

    @Override
    public Score createOrUpdateScore(final String leaderboardNameOrId, final Score score) {

        getValidationHelper().validateModel(score, ValidationGroups.Create.class);

        final long creationTimestamp = currentTimeMillis();

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(score.getProfile());
        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);
        final MongoScoreId mongoScoreId = new MongoScoreId(mongoProfile, mongoLeaderboard);

        final Query<MongoScore> query = getDatastore().createQuery(MongoScore.class);

        query.field("_id").equal(mongoScoreId)
             .field("pointValue").lessThan(score.getPointValue());

        final UpdateOperations<MongoScore> updateOperations = getDatastore().createUpdateOperations(MongoScore.class);
        updateOperations.set("_id", mongoScoreId);
        updateOperations.set("profile", mongoProfile);
        updateOperations.set("leaderboard", mongoLeaderboard);
        updateOperations.set("pointValue", score.getPointValue());
        // Set the timestamp to be "now" on create as well as update since an update essentially resets an existing
        // record
        updateOperations.set("creationTimestamp", creationTimestamp);

        try {
            final MongoScore mongoScore = getDatastore()
                .findAndModify(query, updateOperations, new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true));
            return getBeanMapper().map(mongoScore, Score.class);
        } catch (MongoCommandException ex) {

            // We only get a duplicate exception if the score is less than the provided score.  In which case we simply
            // return the existing score.  All other outcomes will either update or create the score.

            if (ex.getErrorCode() == 11000) {
                final MongoScore mongoScore = getDatastore().get(MongoScore.class, mongoScoreId);
                return getBeanMapper().map(mongoScore, Score.class);
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

}
