package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.sdk.dao.MatchDao;
import dev.getelements.elements.sdk.dao.Matchmaker;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoProfileDao;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.match.MongoMatch;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.TooBusyException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.match.Match;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;

import jakarta.inject.Provider;
import org.bson.types.ObjectId;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import java.sql.Timestamp;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by patricktwohig on 7/25/17.
 */
public class MongoMatchDao implements MatchDao {

    private Datastore datastore;

    private MapperRegistry dozerMapperRegistry;

    private MongoProfileDao mongoProfileDao;

    private MongoDBUtils mongoDBUtils;

    private ValidationHelper validationHelper;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private Provider<Matchmaker> matchmakerProvider;

    private MongoMatchUtils mongoMatchUtils;

    @Override
    public Match getMatchForPlayer(final String playerId, final String matchId) throws NotFoundException {
        final MongoMatch mongoMatch = getMongoMatchForPlayer(playerId, matchId);
        return getDozerMapper().map(mongoMatch, Match.class);
    }

    public MongoMatch getMongoMatchForPlayer(final String playerId, final String matchId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(matchId);
        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);

        final Query<MongoMatch> mongoMatchQuery = getDatastore().find(MongoMatch.class);

        mongoMatchQuery.filter(Filters.and(
                Filters.eq("_id", objectId),
                Filters.eq("player", playerProfile)
        ));

        final MongoMatch mongoMatch = mongoMatchQuery.first();

        if (mongoMatch == null) {
            throw new NotFoundException("match with id " + matchId + " not found.");
        }

        return mongoMatch;

    }

    public MongoMatch getMongoMatch(final String matchId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(matchId);
        final MongoMatch mongoMatch = getDatastore().find(MongoMatch.class)
                .filter(Filters.eq("_id", objectId)).first();

        if (mongoMatch == null) {
            throw new NotFoundException("match with id " + matchId + " not found.");
        }

        return mongoMatch;

    }

    @Override
    public Pagination<Match> getMatchesForPlayer(final String playerId, final int offset, final int count) {

        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);

        final Query<MongoMatch> mongoMatchQuery = getDatastore()
            .find(MongoMatch.class)
            .filter(Filters.eq("player", playerProfile));

        return getMongoDBUtils().paginationFromQuery(mongoMatchQuery, offset, count, m -> getDozerMapper().map(m, Match.class), new FindOptions());

    }

    @Override
    public Pagination<Match> getMatchesForPlayer(String playerId, int offset, int count, String queryString) {
        return Pagination.empty();
    }

    @Override
    public Match createMatch(final Match match) {

        validate(match);

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(match.getPlayer().getId());
        final MongoMatch mongoMatch = getDozerMapper().map(match, MongoMatch.class);

        final Timestamp now = new Timestamp(currentTimeMillis());
        final Timestamp expiry = new Timestamp(now.getTime() + MILLISECONDS.convert(5, MINUTES));

        mongoMatch.setExpiry(expiry);
        mongoMatch.setLastUpdatedTimestamp(now);

        mongoMatch.setPlayer(mongoProfile);
        mongoMatch.setLastUpdatedTimestamp(now);

        getDatastore().save(mongoMatch);

        return getDozerMapper().map(mongoMatch, Match.class);

    }

    @Override
    public void deleteMatch(final String profileId, final String matchId) {

        final MongoMatch toDelete = getMongoMatchForPlayer(profileId, matchId);

        try {
            getMongoConcurrentUtils().performOptimistic(ds -> getMongoMatchUtils().attemptLock(() -> {

                final MongoMatch m = ds.find(MongoMatch.class).filter(Filters.eq("_id", toDelete.getObjectId())).first();

                if (m.getOpponent() != null) {
                    throw new InvalidDataException("Already Matched");
                }

                ds.delete(m);
                return null;

            }, toDelete));
        } catch (MongoConcurrentUtils.ConflictException ex) {
            throw new TooBusyException(ex);
        }

    }

    @Override
    public Matchmaker getDefaultMatchmaker() {
        return getMatchmakerProvider().get();
    }

    public void validate(final Match match) {
        getValidationHelper().validateModel(match);
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }


    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

    public Provider<Matchmaker> getMatchmakerProvider() {
        return matchmakerProvider;
    }

    @Inject
    public void setMatchmakerProvider(Provider<Matchmaker> matchmakerProvider) {
        this.matchmakerProvider = matchmakerProvider;
    }

    public MongoMatchUtils getMongoMatchUtils() {
        return mongoMatchUtils;
    }

    @Inject
    public void setMongoMatchUtils(MongoMatchUtils mongoMatchUtils) {
        this.mongoMatchUtils = mongoMatchUtils;
    }

}
