package com.namazustudios.socialengine.dao.mongo.match;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoProfileDao;
import com.namazustudios.socialengine.dao.mongo.model.match.MongoMatch;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.query.Query;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

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
    public Match getMatchForPlayer(final String playerId, final String matchId) throws NotFoundException {
        final MongoMatch mongoMatch = getMongoMatchForPlayer(playerId, matchId);
        return getDozerMapper().map(mongoMatch, Match.class);
    }

    public MongoMatch getMongoMatchForPlayer(final String playerId, final String matchId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(matchId);
        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);

        final Query<MongoMatch> mongoMatchQuery = getDatastore().createQuery(MongoMatch.class)
            .field("_id").equal(objectId)
            .field("player").equal(playerProfile);

        final MongoMatch mongoMatch = mongoMatchQuery.get();

        if (mongoMatch == null) {
            throw new NotFoundException("match with id " + matchId + " not found.");
        }

        return mongoMatch;

    }

    public MongoMatch getMongoMatch(final String matchId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(matchId);
        final MongoMatch mongoMatch = getDatastore().get(MongoMatch.class, objectId);

        if (mongoMatch == null) {
            throw new NotFoundException("match with id " + matchId + " not found.");
        }

        return mongoMatch;

    }

    @Override
    public Pagination<Match> getMatchesForPlayer(final String playerId, final int offset, final int count) {

        final MongoProfile playerProfile = getMongoProfileDao().getActiveMongoProfile(playerId);

        final Query<MongoMatch> mongoMatchQuery = getDatastore()
            .createQuery(MongoMatch.class)
            .field("player").equal(playerProfile);

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
        getObjectIndex().index(mongoMatch);

        return getDozerMapper().map(mongoMatch, Match.class);

    }

    @Override
    public void deleteMatch(final String profileId, final String matchId) {

        final MongoMatch toDelete = getMongoMatchForPlayer(profileId, matchId);

        try {
            getMongoConcurrentUtils().performOptimistic(ds -> getMongoMatchUtils().attemptLock(() -> {

                final MongoMatch m = ds.get(toDelete);

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
