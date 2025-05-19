package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.dao.mongo.model.match.MongoMatch;
import dev.getelements.elements.sdk.dao.Matchmaker;
import dev.getelements.elements.sdk.model.exception.NoSuitableMatchException;
import dev.getelements.elements.sdk.model.match.Match;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created by patricktwohig on 7/27/17.
 */
public class MongoFIFOMatchmaker implements Matchmaker {

    private Datastore datastore;

    private MongoMatchDao mongoMatchDao;

    private MongoMatchUtils mongoMatchUtils;

    private Consumer<Query<MongoMatch>> applyScope = q -> q.field("scope").doesNotExist();

    @Override
    public SuccessfulMatchTuple attemptToFindOpponent(
            final Match match,
            final int maxCandidatesToConsider,
            final BiFunction<Match, Match, String> finalizer) throws NoSuitableMatchException {

        final Query<MongoMatch> query = getDatastore().find(MongoMatch.class);
        final MongoMatch mongoMatch = getMongoMatchDao().getMongoMatch(match.getId());

        query.filter(Filters.and(
                Filters.eq("player", mongoMatch.getPlayer()).not()),
                Filters.eq("scheme", match.getScheme()),
                Filters.exists("gameId").not(),
                Filters.exists("opponent").not(),
                Filters.exists("lock").not()
        );

        applyScope.accept(query);

        try (var iterator = query.iterator(new FindOptions().sort(Sort.ascending("lastUpdatedTimestamp")))) {
            final List<MongoMatch> mongoMatchList = iterator.toList();
            return getMongoMatchUtils().attemptToPairCandidates(mongoMatch, mongoMatchList, finalizer);
        }

    }

    @Override
    public Matchmaker withScope(final String scope) {

        applyScope = scope == null ?
            q -> q.field("scope").doesNotExist() :
            q -> q.field("scope").equal(scope);

        return this;

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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
