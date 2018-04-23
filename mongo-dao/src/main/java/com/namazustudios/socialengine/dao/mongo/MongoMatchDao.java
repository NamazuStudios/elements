package com.namazustudios.socialengine.dao.mongo;

import com.google.common.collect.Streams;
import com.namazustudios.socialengine.dao.Matchmaker.SuccessfulMatchTuple;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.dao.mongo.model.MongoMatch.MATCH_EXPIRATION_SECONDS;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mongodb.morphia.query.Sort.ascending;

/**
 * Created by patricktwohig on 7/25/17.
 */
public class MongoMatchDao implements MatchDao {

    private AdvancedDatastore datastore;

    private Mapper dozerMapper;

    private MongoProfileDao mongoProfileDao;

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private StandardQueryParser standardQueryParser;

    private ValidationHelper validationHelper;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private Function<MatchingAlgorithm, Matchmaker> matchmakerSupplierFunction;

    private MongoMatchUtils mongoMatchUtils;

    @Override
    public Match getMatchForPlayer(String playerId, String matchId) throws NotFoundException {
        final MongoMatch mongoMatch = getMongoMatchForPlayer(playerId, matchId);
        return getDozerMapper().map(mongoMatch, Match.class);
    }

    public MongoMatch getMongoMatchForPlayer(String playerId, String matchId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(matchId);
        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);

        final Query<MongoMatch> mongoMatchQuery;
        mongoMatchQuery = getDatastore().createQuery(MongoMatch.class);

        mongoMatchQuery.and(
            mongoMatchQuery.criteria("_id").equal(objectId),
            mongoMatchQuery.criteria("player").equal(playerProfile)
        );

        final MongoMatch mongoMatch = mongoMatchQuery.get();

        if (mongoMatch == null) {
            throw new NotFoundException("match with id " + matchId + " not found.");
        }

        return mongoMatch;
    }

    public MongoMatch getMongoMatch(final String matchId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(matchId);

        final Query<MongoMatch> mongoMatchQuery;
        mongoMatchQuery = getDatastore().createQuery(MongoMatch.class);
        mongoMatchQuery.criteria("_id").equal(objectId);

        final MongoMatch mongoMatch = mongoMatchQuery.get();

        if (mongoMatch == null) {
            throw new NotFoundException("match with id " + matchId + " not found.");
        }

        return mongoMatch;

    }

    @Override
    public Pagination<Match> getMatchesForPlayer(String playerId, int offset, int count) {

        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);

        final Query<MongoMatch> mongoMatchQuery;
        mongoMatchQuery = getDatastore().createQuery(MongoMatch.class);

        mongoMatchQuery.and(
                mongoMatchQuery.criteria("player").equal(playerProfile)
        );

        return getMongoDBUtils().paginationFromQuery(mongoMatchQuery, offset, count, m -> getDozerMapper().map(m, Match.class));

    }

    @Override
    public Pagination<Match> getMatchesForPlayer(String playerId, int offset, int count, String queryString) {

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        try {

            final Term playerTerm = new Term("player", playerId);

            booleanQueryBuilder.add(new TermQuery(playerTerm), BooleanClause.Occur.FILTER);
            booleanQueryBuilder.add(getStandardQueryParser().parse(queryString, "player"), BooleanClause.Occur.FILTER);

        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(MongoUser.class, booleanQueryBuilder.build(), offset, count, u -> getDozerMapper().map(u, Match.class));

    }

    @Override
    public TimeDeltaTuple createMatch(Match match) {

        validate(match);

        // Just checks to see if it's
        getMongoProfileDao().getActiveMongoProfile(match.getPlayer().getId());

        // Pre-allocate the match id so we can lock the match in advance.  This prevents other players from matching
        // to it until it's been fully created.

        final ObjectId objectId = new ObjectId();
        final MongoMatch mongoMatch = getDozerMapper().map(match, MongoMatch.class);
        mongoMatch.setObjectId(objectId);

        try {
            return getMongoConcurrentUtils().performOptimistic(ds -> getMongoMatchUtils().attemptLock(() -> doCreateMatchAndLogDelta(mongoMatch), objectId));
        } catch (MongoConcurrentUtils.ConflictException ex) {
            throw new TooBusyException(ex);
        }

    }

    private TimeDeltaTuple doCreateMatchAndLogDelta(final MongoMatch mongoMatch) {

        final Timestamp now = new Timestamp(currentTimeMillis());
        mongoMatch.setLastUpdatedTimestamp(now);

        getMongoDBUtils().perform(ds -> ds.save(mongoMatch));
        getObjectIndex().index(mongoMatch);

        final MongoMatchDelta mongoMatchDelta = new MongoMatchDelta();
        final MongoMatchDelta.Key mongoMatchDeltaKey = new MongoMatchDelta.Key(mongoMatch);

        mongoMatchDelta.setKey(mongoMatchDeltaKey);
        mongoMatchDelta.setOperation(MatchTimeDelta.Operation.CREATED);

        final MongoMatchSnapshot mongoMatchSnapshot = getDozerMapper().map(mongoMatch, MongoMatchSnapshot.class);
        mongoMatchDelta.setSnapshot(mongoMatchSnapshot);

        getMongoDBUtils().perform(ds -> ds.save(mongoMatchDelta));

        return tuple(mongoMatch, mongoMatchDelta);

    }

    private final TimeDeltaTuple tuple(final MongoMatch mongoMatch, final MongoMatchDelta mongoMatchDelta) {
        return new TimeDeltaTuple() {

            @Override
            public Match getMatch() {
                return getDozerMapper().map(mongoMatch, Match.class);
            }

            @Override
            public MatchTimeDelta getTimeDelta() {
                return getDozerMapper().map(mongoMatchDelta, MatchTimeDelta.class);
            }

        };
    }

    @Override
    public MatchTimeDelta deleteMatchAndLogDelta(final String playerId, final String matchId) {

        final Timestamp expiry = new Timestamp(currentTimeMillis());
        final MongoMatch toDelete = getMongoMatchForPlayer(playerId, matchId);

        try {
            getMongoConcurrentUtils().performOptimistic(ds -> getMongoMatchUtils().attemptLock(() -> {

                final MongoMatch m = getMongoMatchForPlayer(playerId, matchId);

                if (m.getOpponent() == null) {
                    ds.delete(m);
                    expireAllDeltasForMatch(new ObjectId(matchId), expiry);
                } else {
                    throw new InvalidDataException("Unable to delete match with opponent assigned.");
                }

                return null;

            }, toDelete));
        } catch (MongoConcurrentUtils.ConflictException ex) {
            throw new TooBusyException(ex);
        }

        final MongoMatchDelta finalMatchDelta = logRemovalDelta(matchId, expiry);
        return getDozerMapper().map(finalMatchDelta, MatchTimeDelta.class);

    }

    private void expireAllDeltasForMatch(final ObjectId matchId, final Timestamp expiry) {

        final Query<MongoMatchDelta> expiryQuery = getDatastore().createQuery(MongoMatchDelta.class);
        expiryQuery.criteria("_id.match").equal(matchId);

        final UpdateOperations<MongoMatchDelta> expiryUpdateOperations;
        expiryUpdateOperations = getDatastore().createUpdateOperations(MongoMatchDelta.class);
        expiryUpdateOperations.set("expiry", expiry);
        getDatastore().update(expiryQuery, expiryUpdateOperations);

    }

    @Override
    public List<MatchTimeDelta> getDeltasForPlayerAfter(final String playerId, final long timeStamp) {

        final Timestamp now = new Timestamp(currentTimeMillis());
        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);
        final Query<MongoMatchDelta> matchTimeDeltaQuery = getDatastore().createQuery(MongoMatchDelta.class);

        matchTimeDeltaQuery.order(ascending("_id.sequence")).and(
            matchTimeDeltaQuery.criteria("_id.timeStamp").greaterThan(new Timestamp(timeStamp)),
            matchTimeDeltaQuery.criteria("snapshot.player").equal(playerProfile),
            matchTimeDeltaQuery.or(
                matchTimeDeltaQuery.criteria("expiry").doesNotExist(),
                matchTimeDeltaQuery.criteria("expiry").lessThanOrEq(now)
            )
        );

        return Streams.stream(matchTimeDeltaQuery)
            .map(mongoMatchDelta -> getDozerMapper().map(mongoMatchDelta, MatchTimeDelta.class))
            .collect(Collectors.toList());

    }

    @Override
    public List<MatchTimeDelta> getDeltasForPlayerAfter(final String playerId,
                                                        final long timeStamp,
                                                        final String matchId) {

        final MongoMatch mongoMatch = getMongoMatchForPlayer(playerId, matchId);
        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);
        final Query<MongoMatchDelta> matchTimeDeltaQuery = getDatastore().createQuery(MongoMatchDelta.class);

        matchTimeDeltaQuery.order(ascending("_id.sequence")).and(
            matchTimeDeltaQuery.criteria("_id.match").equal(mongoMatch.getObjectId()),
            matchTimeDeltaQuery.criteria("_id.timeStamp").greaterThan(new Timestamp(timeStamp)),
            matchTimeDeltaQuery.criteria("snapshot.player").equal(playerProfile)
        );

        return Streams.stream(matchTimeDeltaQuery)
            .map(mongoMatchDelta -> getDozerMapper().map(mongoMatchDelta, MatchTimeDelta.class))
            .collect(Collectors.toList());

    }

    @Override
    public Stream<TimeDeltaTuple> finalize(final SuccessfulMatchTuple successfulMatchTuple, final Supplier<String> finalizer) {

        final long now = currentTimeMillis();

        final Timestamp matchExpiry = new Timestamp(now);
        final Timestamp finalDeltaExpiry = new Timestamp(now + MILLISECONDS.convert(MATCH_EXPIRATION_SECONDS, TimeUnit.SECONDS));

        final ObjectId playerMatchId = new ObjectId(successfulMatchTuple.getPlayerMatch().getId());
        final ObjectId opponentMatchId = new ObjectId(successfulMatchTuple.getOpponentMatch().getId());

        try {
            return getMongoConcurrentUtils().performOptimistic(ds -> getMongoMatchUtils().attemptLock(() -> {

                final MongoMatch playerMatch = getDatastore().get(MongoMatch.class, playerMatchId);
                final MongoMatch opponentMatch = getDatastore().get(MongoMatch.class, opponentMatchId);

                if (playerMatch.getGameId() == null && opponentMatch.getGameId() == null) {

                    final String gameId = finalizer.get();

                    if (gameId == null) {
                        throw new InternalException("Supplied game ID must not be null.");
                    }

                    playerMatch.setExpiry(matchExpiry);
                    playerMatch.setGameId(gameId);
                    opponentMatch.setExpiry(matchExpiry);
                    opponentMatch.setGameId(gameId);

                    getDatastore().save(playerMatch);
                    getDatastore().save(opponentMatch);

                    expireAllDeltasForMatch(playerMatch.getObjectId(), finalDeltaExpiry);
                    expireAllDeltasForMatch(opponentMatch.getObjectId(), finalDeltaExpiry);

                    tuple(playerMatch, logFutureRemovalDelta(playerMatch.getObjectId(), matchExpiry, finalDeltaExpiry));
                    tuple(playerMatch, logFutureRemovalDelta(opponentMatch.getObjectId(), matchExpiry, finalDeltaExpiry));

                    return Stream.of(
                        tuple(playerMatch, getMongoMatchUtils().insertDeltaForUpdate(playerMatch)),
                        tuple(opponentMatch, getMongoMatchUtils().insertDeltaForUpdate(opponentMatch))
                    );

                } else {
                    return Stream.empty();
                }

            }, playerMatchId, opponentMatchId));
        } catch (MongoConcurrentUtils.ConflictException e) {
            throw new TooBusyException(e);
        }

    }

    private MongoMatchDelta logRemovalDelta(final String matchId, final Timestamp expiry) {
        return logRemovalDelta(new ObjectId(matchId), expiry);
    }

    private MongoMatchDelta logRemovalDelta(final ObjectId matchId, final Timestamp expiry) {
        final MongoMatchDelta existing = getMongoMatchUtils().getLatestDelta(matchId);
        final MongoMatchDelta toInsert = new MongoMatchDelta();

        toInsert.setKey(existing.getKey().nextInSequence());
        toInsert.setExpiry(expiry);
        toInsert.setOperation(MatchTimeDelta.Operation.REMOVED);
        toInsert.setSnapshot(null);

        getDatastore().save(toInsert);
        return toInsert;

    }

    private MongoMatchDelta logFutureRemovalDelta(final ObjectId matchId,
                                                  final Timestamp timestamp,
                                                  final Timestamp expiry) {

        final MongoMatchDelta existing = getMongoMatchUtils().getLatestDelta(matchId);
        final MongoMatchDelta toInsert = new MongoMatchDelta();

        toInsert.setKey(existing.getKey().nextInSequence(timestamp.getTime()));
        toInsert.setExpiry(expiry);
        toInsert.setOperation(MatchTimeDelta.Operation.REMOVED);
        toInsert.setSnapshot(null);

        getDatastore().save(toInsert);
        return toInsert;

    }

    @Override
    public Matchmaker getMatchmaker(MatchingAlgorithm matchingAlgorithm) {
        return getMatchmakerSupplierFunction().apply(matchingAlgorithm);
    }

    public void validate(final Match match) {
        getValidationHelper().validateModel(match);
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public StandardQueryParser getStandardQueryParser() {
        return standardQueryParser;
    }

    @Inject
    public void setStandardQueryParser(StandardQueryParser standardQueryParser) {
        this.standardQueryParser = standardQueryParser;
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

    public Function<MatchingAlgorithm, Matchmaker> getMatchmakerSupplierFunction() {
        return matchmakerSupplierFunction;
    }

    @Inject
    public void setMatchmakerSupplierFunction(Function<MatchingAlgorithm, Matchmaker> matchmakerSupplierFunction) {
        this.matchmakerSupplierFunction = matchmakerSupplierFunction;
    }

    public MongoMatchUtils getMongoMatchUtils() {
        return mongoMatchUtils;
    }

    @Inject
    public void setMongoMatchUtils(MongoMatchUtils mongoMatchUtils) {
        this.mongoMatchUtils = mongoMatchUtils;
    }

}
