package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.RankDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoLeaderboard;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoScore;
import com.namazustudios.socialengine.dao.mongo.model.MongoScoreId;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.model.profile.Profile;
import org.dozer.Mapper;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.lang.Math.max;

public class MongoRankDao implements RankDao {

    private Datastore datastore;

    private MongoProfileDao mongoProfileDao;

    private MongoLeaderboardDao mongoLeaderboardDao;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    @Override
    public Pagination<Rank> getRanksForGlobal(final String leaderboardNameOrId,
                                              final int offset, final int count) {

        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);

        final Query<MongoScore> query = getDatastore().createQuery(MongoScore.class);
        query.filter("leaderboard", mongoLeaderboard);



        return getMongoDBUtils().paginationFromQuery(query, offset, count, new Counter(0));

    }

    @Override
    public Pagination<Rank> getRanksForGlobalRelative(final String leaderboardNameOrId, final String profileId,
                                                      final int offset, final int count) {

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profileId);
        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);
        final MongoScoreId mongoScoreId = new MongoScoreId(mongoProfile, mongoLeaderboard);
        final MongoScore mongoScore = getDatastore().get(MongoScore.class, mongoScoreId);

        final Query<MongoScore> query = getDatastore().createQuery(MongoScore.class);
        query.field("leaderboard").equal(mongoLeaderboard);

        final long startIndex;

        if (mongoScore == null) {
            // Asssume player is dead last in the result set because no scores have been submitted.
            startIndex = query.count();
        } else {
            startIndex = query.cloneQuery()
                .field("pointValue").lessThan(mongoScore.getPointValue())
                .count();
        }

        return getMongoDBUtils().paginationFromQuery(query, (int) max(0, offset + startIndex), count, new Counter(0));

    }

    @Override
    public Pagination<Rank> getRanksForFriends(final String leaderboardNameOrId, final Profile profileId,
                                               final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId, final Profile profileId,
                                                       final int offset, final int count) {
        // TODO Needt o merge the friends stuff
        return null;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
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

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    private class Counter implements Function<MongoScore, Rank> {

        private long index;

        public Counter(final long index) {
            this.index = index + 1;
        }

        @Override
        public Rank apply(MongoScore mongoScore) {
            final Rank rank = getDozerMapper().map(mongoScore, Rank.class);
            rank.setPosition(index++);
            return rank;
        }

    }

}
