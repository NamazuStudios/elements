package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoMatch;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;
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
import java.util.List;

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

    @Override
    public Match getMatchForPlayer(String playerId, String matchId) throws NotFoundException {

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

        return getDozerMapper().map(mongoMatch, Match.class);

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
        return null;
    }

    @Override
    public MatchTimeDelta deleteMatchAndLogDelta(String playerId, String matchId) {
        return null;
    }

    @Override
    public List<MatchTimeDelta> getDeltasForPlayerAfter(String playerId, long timeStamp) {
        return null;
    }

    @Override
    public List<MatchTimeDelta> getDeltasForPlayerAfter(String playerId, long timeStamp, String matchId) {
        return null;
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

}
