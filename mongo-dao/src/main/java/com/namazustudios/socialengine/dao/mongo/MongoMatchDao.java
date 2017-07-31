package com.namazustudios.socialengine.dao.mongo;

import com.google.common.collect.Streams;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.fts.ObjectIndex;
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

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
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

        final ObjectId objectId = getMongoDBUtils().parse(matchId);
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

        final ObjectId objectId = getMongoDBUtils().parse(matchId);

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

        final BooleanQuery booleanQuery = new BooleanQuery();

        try {

            final Term playerTerm = new Term("player", playerId);

            booleanQuery.add(new TermQuery(playerTerm), BooleanClause.Occur.FILTER);
            booleanQuery.add(getStandardQueryParser().parse(queryString, "player"), BooleanClause.Occur.FILTER);

        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(MongoUser.class, booleanQuery, offset, count, u -> getDozerMapper().map(u, Match.class));

    }

    @Override
    public TimeDeltaTuple createMatchAndLogDelta(Match match) {

        validate(match);

        // Just checks to see if it's
        getMongoProfileDao().getActiveMongoProfile(match.getPlayer().getId());

        final MongoMatch mongoMatch = getDozerMapper().map(match, MongoMatch.class);

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

        final MongoMatch toDelete = getMongoMatchForPlayer(playerId, matchId);

        try {
            getMongoConcurrentUtils().performOptimistic(ds -> getMongoMatchUtils().attemptLock(() -> {

                final MongoMatch m = getMongoMatchForPlayer(playerId, matchId);

                if (m.getOpponent() == null) {
                    ds.delete(m);
                } else {
                    throw new InvalidDataException("unable to delete match with opponent assigned.");
                }

                return null;

            }, toDelete));
        } catch (MongoConcurrentUtils.ConflictException ex) {
            throw new TooBusyException(ex);
        }

        try {
            return getMongoConcurrentUtils().performOptimisticInsert(ds -> {

                final MongoMatchDelta existing = getMongoMatchUtils().getLatestDelta(matchId);
                final MongoMatchDelta toInsert = new MongoMatchDelta();

                if (existing == null) {
                    final MongoMatchDelta.Key mongoMatchDeltaKey = new MongoMatchDelta.Key(toDelete.getObjectId());
                    toInsert.setKey(mongoMatchDeltaKey);
                } else {
                    toInsert.setKey(existing.getKey().nextInSequence());
                }

                toInsert.setOperation(MatchTimeDelta.Operation.REMOVED);
                toInsert.setSnapshot(null);

                getDatastore().save(toInsert);

                return getDozerMapper().map(toInsert, MatchTimeDelta.class);

            });
        } catch (MongoConcurrentUtils.ConflictException e) {
            throw new TooBusyException(e);
        }

    }

    @Override
    public List<MatchTimeDelta> getDeltasForPlayerAfter(String playerId, long timeStamp) {

        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);
        final Query<MongoMatchDelta> matchTimeDeltaQuery = getDatastore().createQuery(MongoMatchDelta.class);

        matchTimeDeltaQuery.order(ascending("_id.sequence")).and(
            matchTimeDeltaQuery.criteria("_id.timeStamp").greaterThanOrEq(timeStamp),
            matchTimeDeltaQuery.criteria("snapshot.player").equal(playerProfile)
        );

        return Streams.stream(matchTimeDeltaQuery)
            .map(mongoMatchDelta -> getDozerMapper().map(mongoMatchDelta, MatchTimeDelta.class))
            .collect(Collectors.toList());

    }

    @Override
    public List<MatchTimeDelta> getDeltasForPlayerAfter(String playerId, long timeStamp, String matchId) {

        final MongoMatch mongoMatch = getMongoMatchForPlayer(playerId, matchId);
        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);
        final Query<MongoMatchDelta> matchTimeDeltaQuery = getDatastore().createQuery(MongoMatchDelta.class);

        matchTimeDeltaQuery.order(ascending("sequence")).and(
            matchTimeDeltaQuery.criteria("_id.match").equal(mongoMatch.getObjectId()),
            matchTimeDeltaQuery.criteria("_id.timeStamp").greaterThanOrEq(timeStamp),
            matchTimeDeltaQuery.criteria("snapshot.player").equal(playerProfile)
        );

        return Streams.stream(matchTimeDeltaQuery)
            .map(mongoMatchDelta -> getDozerMapper().map(mongoMatchDelta, MatchTimeDelta.class))
            .collect(Collectors.toList());

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
