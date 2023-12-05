package dev.getelements.elements.dao.mongo;

import com.mongodb.DBRef;
import dev.getelements.elements.dao.RankDao;
import dev.getelements.elements.dao.mongo.model.*;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.leaderboard.Rank;
import dev.getelements.elements.model.leaderboard.Score;
import dev.morphia.Datastore;
import dev.morphia.aggregation.Aggregation;
import dev.morphia.aggregation.expressions.ComparisonExpressions;
import dev.morphia.aggregation.stages.*;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import org.bson.Document;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.morphia.aggregation.expressions.Expressions.*;
import static dev.morphia.aggregation.stages.Facet.facet;
import static dev.morphia.aggregation.stages.Limit.limit;
import static dev.morphia.aggregation.stages.Lookup.lookup;
import static dev.morphia.aggregation.stages.Match.match;
import static dev.morphia.aggregation.stages.Projection.project;
import static dev.morphia.aggregation.stages.ReplaceRoot.replaceRoot;
import static dev.morphia.aggregation.stages.Skip.skip;
import static dev.morphia.aggregation.stages.Sort.sort;
import static dev.morphia.aggregation.stages.Unwind.unwind;
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

        final var profiles = getMongoFriendDao()
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
        return Pagination.empty();

//        final var optionalMongoProfile = getMongoProfileDao().findActiveMongoProfile(profileId);
//
//        if (optionalMongoProfile.isEmpty()) {
//            return Pagination.empty();
//        }
//
//        final MongoProfile mongoProfile = optionalMongoProfile.get();
//
//        final List<MongoProfile> profiles = getMongoFollowerDao()
//                .getMutualMongoFollowers(mongoProfile)
//                .stream()
//                .map(follower -> follower.getObjectId().getFollowedId())
//                .map(getMongoProfileDao()::getActiveMongoProfile)
//                .collect(toList());
//
//        profiles.add(mongoProfile);
//
//        return getRanks(
//                leaderboardNameOrId,
//                offset, count,
//                leaderboardEpoch,
//                q -> q.filter(in("profile", profiles))
//        );

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

        final var mongoProfile = optionalMongoProfile.get();
        final var mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);

        final Supplier<Aggregation<?>> aggregationSupplier = () -> aggregateScoresForMutualFollowers(
            mongoProfile,
            mongoLeaderboard,
            leaderboardEpoch
        );

        final var rank = aggregateRankForMutualFollowers(mongoProfile, mongoLeaderboard, leaderboardEpoch);

        final long adjustedOffset = max(0, (offset + rank) - (count / 2));

        return getMongoDBUtils().paginationFromAggregation(
                aggregationSupplier,
                MongoScore.class,
                offset, count
        ).transform(new Counter(adjustedOffset));

    }

    private int aggregateRankForMutualFollowers(
            final MongoProfile mongoProfile,
            final MongoLeaderboard mongoLeaderboard,
            final long leaderboardEpoch) {

        final var calculatedEpoch = calculateEpoch(mongoLeaderboard, leaderboardEpoch);
        final var leaderboardDBRef = new DBRef("leaderboard", mongoLeaderboard.getObjectId());
        final var leaderboardEpochLookupValue = calculateEpoch(mongoLeaderboard, leaderboardEpoch);
        final var mongoScoreId = new MongoScoreId(mongoProfile, mongoLeaderboard, leaderboardEpochLookupValue);

        final var mongoScore = getDatastore()
                .find(MongoScore.class)
                .filter(eq("_id", mongoScoreId))
                .first();

        if (mongoScore == null) {
            return 0;
        }

        final var aggregate = getMongoFollowerDao().aggregateMutualFollowers(mongoProfile)
                .lookup(lookup(MongoScore.class)
                        .as("score")
                        .let("followerId", field("_id.profileId"))
                        .pipeline(
                                match(
                                        eq("leaderboard", leaderboardDBRef),
                                        eq("leaderboardEpoch", calculatedEpoch),
                                        gte("pointValue", mongoScore.getPointValue()),
                                        expr(
                                                ComparisonExpressions.eq(
                                                        field("_id.profileId"),
                                                        value("$$followerId")
                                                )
                                        )
                                )
                        )
                )
                .unwind(unwind("score")).replaceRoot(replaceRoot(field("score")))
//                .count("count")
//                .unwind(unwind("count"))
                ;

        try (var cursor = aggregate.execute(Document.class)) {
            final var list = cursor.toList();
            return list.isEmpty() ? 0 : list.get(0).get("count", Number.class).intValue();
        }

    }

    private Aggregation<?> aggregateScoresForMutualFollowers(
            final MongoProfile mongoProfile,
            final MongoLeaderboard mongoLeaderboard,
            final long leaderboardEpoch) {

        final var calculatedEpoch = calculateEpoch(mongoLeaderboard, leaderboardEpoch);
        final var leaderboardDBRef = new DBRef("leaderboard", mongoLeaderboard.getObjectId());

        final var aggregate = getMongoFollowerDao().aggregateMutualFollowers(mongoProfile);

        return getMongoFollowerDao().aggregateMutualFollowers(mongoProfile)
                .lookup(lookup(MongoScore.class)
                        .as("score")
                        .let("followerId", field("_id.profileId"))
                        .pipeline(
                                match(
                                        eq("leaderboard", leaderboardDBRef),
                                        eq("leaderboardEpoch", calculatedEpoch),
                                        expr(
                                                ComparisonExpressions.eq(
                                                        field("_id.profileId"),
                                                        value("$$followerId")
                                                )
                                        )
                                )
                        )
                )
                .unwind(unwind("score")).replaceRoot(replaceRoot(field("score")))
                .sort(sort().descending("pointValue"));
    }

    public Pagination<Rank> getRanks(
            final String leaderboardNameOrId,
            final int offset, final int count,
            final long leaderboardEpoch,
            final Function<Query<MongoScore>, Query<MongoScore>> queryTransformer) {

        final var mongoLeaderboard = getMongoLeaderboardDao().getMongoLeaderboard(leaderboardNameOrId);

        final var query = queryTransformer
                .apply(getDatastore().find(MongoScore.class))
                .filter(eq("leaderboard", mongoLeaderboard))
                .filter(eq("leaderboardEpoch", calculateEpoch(mongoLeaderboard, leaderboardEpoch)));

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
        final var leaderboardEpochLookupValue = calculateEpoch(mongoLeaderboard, leaderboardEpoch);
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
                new Counter(adjustedOffset),
                new FindOptions().sort(descending("pointValue"))
        );

    }

    private long calculateEpoch(final MongoLeaderboard mongoLeaderboard, final long leaderboardEpoch ) {
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

    public static class MongoRankPagination extends Pagination<MongoScore> {}

}
