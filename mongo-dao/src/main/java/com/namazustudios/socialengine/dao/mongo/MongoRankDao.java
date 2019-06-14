package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.RankDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoLeaderboard;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoScore;
import com.namazustudios.socialengine.dao.mongo.model.MongoScoreId;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.leaderboard.Rank;
import com.namazustudios.socialengine.model.leaderboard.Score;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Expose;
import org.dozer.Mapper;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

public class MongoRankDao implements RankDao {

    private Datastore datastore;

    private MongoProfileDao mongoProfileDao;

    private MongoLeaderboardDao mongoLeaderboardDao;

    private MongoFriendDao mongoFriendDao;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    @Override
    public Pagination<Rank> getRanksForGlobal(final String leaderboardNameOrId,
                                              final int offset, final int count, final long leaderboardEpoch) {

        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);

        final long leaderboardEpochLookupValue;

        switch (mongoLeaderboard.getTimeStrategyType()) {
            case ALL_TIME:
                leaderboardEpochLookupValue = MongoScoreId.ALL_TIME_LEADERBOARD_EPOCH;
                break;
            case EPOCHAL:
                if (leaderboardEpoch > 0) {
                    leaderboardEpochLookupValue = mongoLeaderboard.getEpochForMillis(leaderboardEpoch);
                }
                else {
                    leaderboardEpochLookupValue = mongoLeaderboard.getCurrentEpoch();
                }
                break;
            default:
                throw new IllegalStateException("Invalid time strategy type.");
        }

        final Query<MongoScore> query = getDatastore().createQuery(MongoScore.class);
        query
            .filter("leaderboard", mongoLeaderboard)
            .filter("leaderboardEpoch",
                    leaderboardEpochLookupValue > 0 ? leaderboardEpochLookupValue : mongoLeaderboard.getCurrentEpoch())
            .order(Sort.descending("pointValue"));

        final long adjustedOffset = max(0, offset);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, new Counter(adjustedOffset));

    }

    @Override
    public Pagination<Rank> getRanksForGlobalRelative(final String leaderboardNameOrId, final String profileId,
                                                      final int count, final long leaderboardEpoch) {

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profileId);
        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);

        final long leaderboardEpochLookupValue;

        switch (mongoLeaderboard.getTimeStrategyType()) {
            case ALL_TIME:
                leaderboardEpochLookupValue = MongoScoreId.ALL_TIME_LEADERBOARD_EPOCH;
                break;
            case EPOCHAL:
                if (leaderboardEpoch > 0) {
                    leaderboardEpochLookupValue = mongoLeaderboard.getEpochForMillis(leaderboardEpoch);
                }
                else {
                    leaderboardEpochLookupValue = mongoLeaderboard.getCurrentEpoch();
                }
                break;
            default:
                throw new IllegalStateException("Invalid time strategy type.");
        }

        final MongoScoreId mongoScoreId = new MongoScoreId(mongoProfile, mongoLeaderboard, leaderboardEpochLookupValue);
        final MongoScore mongoScore = getDatastore().get(MongoScore.class, mongoScoreId);

        final Query<MongoScore> query = getDatastore()
            .createQuery(MongoScore.class)
            .field("leaderboard").equal(mongoLeaderboard)
            .field("leaderboardEpoch").equal(leaderboardEpochLookupValue)
            .order(Sort.descending("pointValue"));

        final long playerRank = mongoScore == null ? 0 : query
            .cloneQuery()
            .field("pointValue").greaterThan(mongoScore.getPointValue())
            .count();

        final long offset = Math.max(0, playerRank - count/2);
        return getMongoDBUtils().paginationFromQuery(query, (int) offset, count, new Counter(offset));

    }

    @Override
    public Pagination<Rank> getRanksForFriends(final String leaderboardNameOrId, final Profile profileId,
                                               final int offset, final int count, final long leaderboardEpoch) {

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profileId);
        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);

        final List<MongoProfile> profiles = getMongoFriendDao()
            .getAllMongoFriendshipsForUser(mongoProfile.getUser())
            .stream()
            .map(friendship -> friendship.getObjectId().getOpposite(mongoProfile.getUser().getObjectId()))
            .flatMap(userId -> getMongoProfileDao().getActiveMongoProfilesForUser(userId))
            .collect(toList());

        profiles.add(mongoProfile);

        final long leaderboardEpochLookup;

        final long leaderboardEpochLookupValue;

        switch (mongoLeaderboard.getTimeStrategyType()) {
            case ALL_TIME:
                leaderboardEpochLookupValue = MongoScoreId.ALL_TIME_LEADERBOARD_EPOCH;
                break;
            case EPOCHAL:
                if (leaderboardEpoch > 0) {
                    leaderboardEpochLookupValue = mongoLeaderboard.getEpochForMillis(leaderboardEpoch);
                }
                else {
                    leaderboardEpochLookupValue = mongoLeaderboard.getCurrentEpoch();
                }
                break;
            default:
                throw new IllegalStateException("Invalid time strategy type.");
        }

        final Query<MongoScore> query = getDatastore().createQuery(MongoScore.class);

        query.field("profile").in(profiles)
             .field("leaderboard").equal(mongoLeaderboard)
             .field("leaderboardEpoch").equal(leaderboardEpochLookupValue)
             .order(Sort.descending("pointValue"));

        final long adjustedOffset = max(0, offset);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, new Counter(adjustedOffset));
    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId, final Profile profileId,
                                                       final int offset, final int count, final long leaderboardEpoch) {

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profileId);
        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);
        final MongoScoreId mongoScoreId = new MongoScoreId(mongoProfile, mongoLeaderboard);
        final MongoScore mongoScore = getDatastore().get(MongoScore.class, mongoScoreId);

        final List<MongoProfile> profiles = getMongoFriendDao()
            .getAllMongoFriendshipsForUser(mongoProfile.getUser())
            .stream()
            .map(friendship -> friendship.getObjectId().getOpposite(mongoProfile.getUser().getObjectId()))
            .flatMap(userId -> getMongoProfileDao().getActiveMongoProfilesForUser(userId))
            .collect(toList());

        profiles.add(mongoProfile);

        final long leaderboardEpochLookupValue;

        switch (mongoLeaderboard.getTimeStrategyType()) {
            case ALL_TIME:
                leaderboardEpochLookupValue = MongoScoreId.ALL_TIME_LEADERBOARD_EPOCH;
                break;
            case EPOCHAL:
                if (leaderboardEpoch > 0) {
                    leaderboardEpochLookupValue = mongoLeaderboard.getEpochForMillis(leaderboardEpoch);
                }
                else {
                    leaderboardEpochLookupValue = mongoLeaderboard.getCurrentEpoch();
                }
                break;
            default:
                throw new IllegalStateException("Invalid time strategy type.");
        }

        final Query<MongoScore> query = getDatastore().createQuery(MongoScore.class);

        query.field("leaderboard").equal(mongoLeaderboard)
             .field("leaderboardEpoch").equal(leaderboardEpochLookupValue)
             .field("profile").in(profiles)
             .order(Sort.descending("pointValue"));

        final long playerRank = mongoScore == null ? 0 : query
            .cloneQuery()
            .field("profile").notEqual(mongoProfile)
            .field("pointValue").greaterThan(mongoScore.getPointValue())
            .count();

        final long adjustedOffset = max(0, offset + playerRank);
        return getMongoDBUtils().paginationFromQuery(query, (int) adjustedOffset, count, new Counter(adjustedOffset));

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

    public MongoFriendDao getMongoFriendDao() {
        return mongoFriendDao;
    }

    @Inject
    public void setMongoFriendDao(MongoFriendDao mongoFriendDao) {
        this.mongoFriendDao = mongoFriendDao;
    }

    private class Counter implements Function<MongoScore, Rank> {

        private long index;

        public Counter(final long index) {
            this.index = index;
        }

        @Override
        public Rank apply(MongoScore mongoScore) {

            final Rank rank = getDozerMapper().map(mongoScore, Rank.class);
            rank.setPosition(++index);

            final Score score = getDozerMapper().map(mongoScore, Score.class);
            rank.setScore(score);

            return rank;

        }

    }

}
