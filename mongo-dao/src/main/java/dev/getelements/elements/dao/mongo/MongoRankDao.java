package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.RankDao;
import dev.getelements.elements.dao.mongo.model.MongoLeaderboard;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.MongoScore;
import dev.getelements.elements.dao.mongo.model.MongoScoreId;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.leaderboard.Rank;
import dev.getelements.elements.model.leaderboard.Score;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

import static dev.morphia.query.Sort.descending;
import static dev.morphia.query.filters.Filters.*;
import static java.lang.Math.max;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class MongoRankDao implements RankDao {

    private Datastore datastore;

    private MongoProfileDao mongoProfileDao;

    private MongoLeaderboardDao mongoLeaderboardDao;

    private MongoFriendDao mongoFriendDao;

    private MongoFollowerDao mongoFollowerDao;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    @Override
    public Pagination<Rank> getRanksForGlobal(final String leaderboardNameOrId,
                                              final int offset, final int count,
                                              final long leaderboardEpoch) {
        return getRanks(leaderboardNameOrId, offset, count, leaderboardEpoch, identity());
    }

    @Override
    public Pagination<Rank> getRanksForGlobalRelative(final String leaderboardNameOrId,
                                                      final String profileId,
                                                      final int offset, final int count,
                                                      final long leaderboardEpoch) {
        final var optionalMongoProfile = getMongoProfileDao().findActiveMongoProfile(profileId);

        if (optionalMongoProfile.isEmpty()) {
            return Pagination.empty();
        }

        final MongoProfile mongoProfile = optionalMongoProfile.get();

        return getRanksRelative(
                leaderboardNameOrId,
                mongoProfile,
                offset, count,
                leaderboardEpoch,
                identity(),
                identity()
        );
    }

    @Override
    public Pagination<Rank> getRanksForFriends(final String leaderboardNameOrId,
                                               final String  profileId,
                                               final int offset, final int count,
                                               final long leaderboardEpoch) {

        final var optionalMongoProfile = getMongoProfileDao().findActiveMongoProfile(profileId);

        if (optionalMongoProfile.isEmpty()) {
            return Pagination.empty();
        }

        final MongoProfile mongoProfile = optionalMongoProfile.get();

        final List<MongoProfile> profiles = getMongoFriendDao()
            .getAllMongoFriendshipsForUser(mongoProfile.getUser())
            .stream()
            .map(friendship -> friendship.getObjectId().getOpposite(mongoProfile.getUser().getObjectId()))
            .flatMap(userId -> getMongoProfileDao().getActiveMongoProfilesForUser(userId))
            .collect(toList());

        profiles.add(mongoProfile);

        return getRanks(
                leaderboardNameOrId,
                offset, count,
                leaderboardEpoch,
                query -> query.filter(in("profile", profiles))
        );

    }

    @Override
    public Pagination<Rank> getRanksForFriendsRelative(final String leaderboardNameOrId,
                                                       final String profileId,
                                                       final int offset, final int count,
                                                       final long leaderboardEpoch) {

        final var optionalMongoProfile = getMongoProfileDao().findActiveMongoProfile(profileId);

        if (optionalMongoProfile.isEmpty()) {
            return Pagination.empty();
        }

        final MongoProfile mongoProfile = optionalMongoProfile.get();

        final List<MongoProfile> profiles = getMongoFriendDao()
            .getAllMongoFriendshipsForUser(mongoProfile.getUser())
            .stream()
            .map(friendship -> friendship.getObjectId().getOpposite(mongoProfile.getUser().getObjectId()))
            .flatMap(userId -> getMongoProfileDao().getActiveMongoProfilesForUser(userId))
            .collect(toList());

        profiles.add(mongoProfile);

        return getRanksRelative(
                leaderboardNameOrId,
                mongoProfile,
                offset, count,
                leaderboardEpoch,
                q -> q.filter(in("profile", profiles)),
                q -> q.filter(ne("profile", mongoProfile))
        );

    }

    @Override
    public Pagination<Rank> getRanksForMutualFollowers(final String leaderboardNameOrId,
                                                       final String  profileId,
                                                       final int offset, final int count,
                                                       final long leaderboardEpoch) {

        final var optionalMongoProfile = getMongoProfileDao().findActiveMongoProfile(profileId);

        if (optionalMongoProfile.isEmpty()) {
            return Pagination.empty();
        }

        final MongoProfile mongoProfile = optionalMongoProfile.get();

        final List<MongoProfile> profiles = getMongoFollowerDao()
                .getMutualMongoFollowers(mongoProfile)
                .stream()
                .map(follower -> follower.getObjectId().getFollowedId())
                .map(getMongoProfileDao()::getActiveMongoProfile)
                .collect(toList());


        profiles.add(mongoProfile);

        return getRanks(
                leaderboardNameOrId,
                offset, count,
                leaderboardEpoch,
                q -> q.filter(in("profile", profiles))
        );

    }

    @Override
    public Pagination<Rank> getRanksForMutualFollowersRelative(final String leaderboardNameOrId,
                                                               final String profileId,
                                                               final int offset, final int count,
                                                               final long leaderboardEpoch) {

        final var optionalMongoProfile = getMongoProfileDao().findActiveMongoProfile(profileId);

        if (optionalMongoProfile.isEmpty()) {
            return Pagination.empty();
        }

        final MongoProfile mongoProfile = optionalMongoProfile.get();

        final List<MongoProfile> profiles = getMongoFollowerDao()
                .getMutualMongoFollowers(mongoProfile)
                .stream()
                .map(follower -> follower.getObjectId().getFollowedId())
                .map(getMongoProfileDao()::getActiveMongoProfile)
                .collect(toList());


        profiles.add(mongoProfile);

        return getRanksRelative(
                leaderboardNameOrId,
                mongoProfile,
                offset, count,
                leaderboardEpoch,
                q -> q.filter(in("profile", profiles)),
                q -> q.filter(ne("profile", mongoProfile))
        );

    }

    public Pagination<Rank> getRanks(
            final String leaderboardNameOrId,
            final int offset, final int count,
            final long leaderboardEpoch,
            final Function<Query<MongoScore>, Query<MongoScore>> queryTransformer) {

        final var mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);
        final var leaderboardEpochLookupValue = calculateEpochLookupValue(mongoLeaderboard, leaderboardEpoch);

        final var query = queryTransformer
                .apply(getDatastore().find(MongoScore.class))
                .filter(eq("leaderboard", mongoLeaderboard))
                .filter(eq("leaderboardEpoch", leaderboardEpochLookupValue > 0
                                ? leaderboardEpochLookupValue
                                : mongoLeaderboard.calculateCurrentEpoch()));

        final long adjustedOffset = max(0, offset);

        return getMongoDBUtils().paginationFromQuery(
                query,
                offset, count,
                new Counter(adjustedOffset),
                new FindOptions().sort(descending("pointValue"))
        );

    }

    public Pagination<Rank> getRanksRelative(
            final String leaderboardNameOrId,
            final MongoProfile mongoProfile,
            final int offset, final int count,
            final long leaderboardEpoch,
            final Function<Query<MongoScore>, Query<MongoScore>> queryTransformer,
            final Function<Query<MongoScore>, Query<MongoScore>> countQueryTransformer) {

        final var mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);
        final var leaderboardEpochLookupValue = calculateEpochLookupValue(mongoLeaderboard, leaderboardEpoch);
        final var mongoScoreId = new MongoScoreId(mongoProfile, mongoLeaderboard, leaderboardEpochLookupValue);

        final var mongoScore = getDatastore()
                .find(MongoScore.class)
                .filter(eq("_id", mongoScoreId))
                .first();

        final var query = queryTransformer
                .apply(getDatastore().find(MongoScore.class))
                .filter(eq("leaderboard", mongoLeaderboard))
                .filter(eq("leaderboardEpoch", leaderboardEpochLookupValue));

        final var rank = mongoScore == null
                ? 0
                : countQueryTransformer.apply(getDatastore().find(MongoScore.class))
                    .filter(eq("leaderboard", mongoLeaderboard))
                    .filter(eq("leaderboardEpoch", leaderboardEpochLookupValue))
                    .filter(gte("pointValue", mongoScore.getPointValue()))
                    .count();

        final long adjustedOffset = max(0, (offset + rank) - (count / 2));

        return getMongoDBUtils().paginationFromQuery(
                query,
                (int) adjustedOffset, count,
                new Counter(offset),
                new FindOptions().sort(descending("pointValue"))
        );

    }

    private long calculateEpochLookupValue(final MongoLeaderboard mongoLeaderboard, final long leaderboardEpoch ) {
        switch (mongoLeaderboard.getTimeStrategyType()) {
            case ALL_TIME:
                return MongoScoreId.ALL_TIME_LEADERBOARD_EPOCH;
            case EPOCHAL:
                return leaderboardEpoch > 0
                        ? mongoLeaderboard.calculateEpochForMillis(leaderboardEpoch)
                        : mongoLeaderboard.calculateCurrentEpoch();
            default:
                throw new IllegalArgumentException("Invalid time strategy type.");
        }

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

    public MongoFollowerDao getMongoFollowerDao() {
        return mongoFollowerDao;
    }

    @Inject
    public void setMongoFollowerDao(MongoFollowerDao mongoFollowerDao) {
        this.mongoFollowerDao = mongoFollowerDao;
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
