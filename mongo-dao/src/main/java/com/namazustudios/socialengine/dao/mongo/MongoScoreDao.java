package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.ScoreDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoLeaderboard;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoScore;
import com.namazustudios.socialengine.dao.mongo.model.MongoScoreId;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.model.leaderboard.Score;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.dozer.Mapper;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

public class MongoScoreDao implements ScoreDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MongoProfileDao mongoProfileDao;

    private MongoLeaderboardDao mongoLeaderboardDao;

    private Mapper beanMapper;

    @Override
    public Score createOrUpdateScore(final String leaderboardNameOrId, final Score score) {

        getValidationHelper().validateModel(score, ValidationGroups.Create.class);

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(score.getProfile());
        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);
        final MongoScoreId mongoScoreId = new MongoScoreId(mongoProfile, mongoLeaderboard);

        final Query<MongoScore> query = getDatastore().createQuery(MongoScore.class);
        query.field("_id").equal(mongoScoreId);

        final UpdateOperations<MongoScore> updateOperations = getDatastore().createUpdateOperations(MongoScore.class);
        updateOperations.set("_id", mongoScoreId);
        updateOperations.set("profile", mongoProfile);
        updateOperations.set("leaderboard", mongoLeaderboard);
        updateOperations.set("scoreValue", score.getPointValue());

        final MongoScore mongoScore = getDatastore().findAndModify(query, updateOperations, new FindAndModifyOptions()
            .returnNew(true)
            .upsert(true));

        return getBeanMapper().map(mongoScore, Score.class);

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
