package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.mongo.model.MongoMatch;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;

import javax.inject.Inject;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by patricktwohig on 7/27/17.
 */
public class MongoFIFOMatchmaker implements Matchmaker {

    private AdvancedDatastore datastore;

    private MongoMatchDao mongoMatchDao;

    private MongoMatchUtils mongoMatchUtils;

    @Override
    public MatchingAlgorithm getAlgorithm() {
        return MatchingAlgorithm.FIFO;
    }

    @Override
    public SuccessfulMatchTuple attemptToFindOpponent(
            final Match match,
            final int maxCandidatesToConsider,
            final BiFunction<Match, Match, String> finalizer) throws NoSuitableMatchException {

        final Query<MongoMatch> query = getDatastore().createQuery(MongoMatch.class);
        final MongoMatch mongoMatch = getMongoMatchDao().getMongoMatch(match.getId());

        query.order(Sort.ascending("lastUpdatedTimestamp"))
             .field("player").notEqual(mongoMatch.getPlayer())
             .field("scheme").equal(match.getScheme())
             .field("gameId").doesNotExist()
             .field("opponent").doesNotExist()
             .field("lock").doesNotExist();

        final FindOptions findOptions = new FindOptions().limit(maxCandidatesToConsider);
        final List<MongoMatch> mongoMatchList = query.asList(findOptions);
        return getMongoMatchUtils().attemptToPairCandidates(mongoMatch, mongoMatchList, finalizer);

    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public MongoMatchDao getMongoMatchDao() {
        return mongoMatchDao;
    }

    @Inject
    public void setMongoMatchDao(MongoMatchDao mongoMatchDao) {
        this.mongoMatchDao = mongoMatchDao;
    }

    public MongoMatchUtils getMongoMatchUtils() {
        return mongoMatchUtils;
    }

    @Inject
    public void setMongoMatchUtils(MongoMatchUtils mongoMatchUtils) {
        this.mongoMatchUtils = mongoMatchUtils;
    }

}
