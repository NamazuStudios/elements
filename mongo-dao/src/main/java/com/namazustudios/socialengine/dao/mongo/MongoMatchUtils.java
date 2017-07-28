package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoException;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.mongo.model.MongoMatch;
import com.namazustudios.socialengine.dao.mongo.model.MongoMatchDelta;
import com.namazustudios.socialengine.dao.mongo.model.MongoMatchLock;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.mongodb.morphia.query.Sort.descending;

/**
 * Used to actually perform the match between two players.  This ensures that two matches are made
 * with consistency and all appropriate diffs are created.
 *
 * Created by patricktwohig on 7/27/17.
 */
public class MongoMatchUtils {

    private AdvancedDatastore datastore;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private MongoConcurrentUtils mongoConcurrentUtils;

    /**
     * Intended to be used in the scope of {@link MongoConcurrentUtils#performOptimistic(MongoConcurrentUtils.CriticalOperation)},
     * this will attempt to lock the {@link MongoMatch}.  If successful, this will perform the operation defined by
     * the supplied {@link Provider<T>} instance.  If the lock fails, then this will throw an instance of
     * {@link MongoConcurrentUtils.ContentionException} to indicate that a re-lock must be attempted.
     *
     * This is necessary because matching may happen at any time which involves controlling access to
     * multiple documents at a time.  Therefore code which mutates or deletes a {@link MongoMatch} should only
     * be done within the context of a lock.
     *
     * @param resultProvider the {@link Provider<T>} defining the operation to perform
     * @param mongoMatches one or {@link MongoMatch} instances to return
     * @param <T> the return type
     * @return the value retuend by the supplied provider
     * @throws MongoConcurrentUtils.ContentionException if locking fails
     */
    public <T> T attemptLock(
            final Provider<T> resultProvider,
            final MongoMatch ... mongoMatches) throws MongoConcurrentUtils.ContentionException {

        final List<MongoMatchLock> matchLockList = stream(mongoMatches)
            .map(m -> new MongoMatchLock(m.getObjectId()))
            .collect(Collectors.toList());

        try {
            getDatastore().insert(matchLockList);
        } catch (MongoException ex) {
            if (ex.getCode() == 11000) {

                // The only expected exception here is the duplicate key exception, which
                // will be cleaned up in the above code's finally block.  If another exception
                // is thrown then we have to assume this isn't just an expected problem with
                // matching, but rather an actual exception.

                throw new MongoConcurrentUtils.ContentionException(ex);

            } else {
                throw new InternalException(ex);
            }
        }

        try {
            return resultProvider.get();
        } finally {
            final Query<MongoMatchLock> query = getDatastore().createQuery(MongoMatchLock.class);
            query.field("_id").hasAnyOf(stream(mongoMatches).map(m -> m.getObjectId()).collect(Collectors.toList()));
            getDatastore().delete(query);
        }

    }

    public Matchmaker.SuccessfulMatchTuple attemptToPairCandidates(
            final MongoMatch playerMatch,
            final List<MongoMatch> candidatesList) throws NoSuitableMatchException {

        for (final MongoMatch candidateMatch : candidatesList) {

            if (Objects.equals(playerMatch.getObjectId(), candidateMatch.getObjectId())) {
                continue;
            }

            return attemptToPairCandidates(playerMatch, candidateMatch);

        }

        throw new NoSuitableMatchException("no suitable matches found");

    }

    /**
     * Attempts to pair the two matches in the database.  If this succeeds, an instance
     * of {@link Matchmaker.SuccessfulMatchTuple} will be returned.  If not an instance
     * of {@link NoSuitableMatchException} will be thrown.
     *
     * @param playerMatch the player to match to the opponent
     * @param opponentMatch the opponent to match to the player
     *
     * @return an instance of {@link Matchmaker.SuccessfulMatchTuple} complete with delta log
     *
     * @throws NoSuitableMatchException if the matching fails
     */
    public Matchmaker.SuccessfulMatchTuple attemptToPairCandidates(
            final MongoMatch playerMatch,
            final MongoMatch opponentMatch) throws NoSuitableMatchException {

        final List<MongoMatchLock> mongoMatchList;

        mongoMatchList = asList(
                new MongoMatchLock(playerMatch.getObjectId()),
                new MongoMatchLock(opponentMatch.getObjectId()));

        try {
            getDatastore().insert(mongoMatchList);
        } catch (MongoException ex) {
            if (ex.getCode() == 11000) {
                throw new NoSuitableMatchException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        try {
            return doAttempt(playerMatch, opponentMatch);
        } finally {
            final Query<MongoMatchLock> query = getDatastore().createQuery(MongoMatchLock.class);
            query.field("_id").hasAnyOf(mongoMatchList);
            getDatastore().delete(query);
        }

    }

    private Matchmaker.SuccessfulMatchTuple doAttempt(MongoMatch playerMatch,
                                                      MongoMatch opponentMatch) throws NoSuitableMatchException {

        // Now that both entities are locked, we can now update both objects to match player and
        // opponent together, driving any deltas as necessary.

        final Query<MongoMatch> playerQuery = getDatastore()
            .createQuery(MongoMatch.class)
            .field("_id").equal(playerMatch.getObjectId());

        final Query<MongoMatch> opponentQuery = getDatastore()
            .createQuery(MongoMatch.class)
            .field("_id").equal(opponentMatch.getObjectId());

        playerMatch = playerQuery.get();
        opponentMatch = opponentQuery.get();

        if (playerMatch == null || opponentMatch == null) {
            throw new NoSuitableMatchException("player or opponent not found");
        } else if (playerMatch.getOpponent() != null || opponentMatch.getOpponent() != null) {
            throw new NoSuitableMatchException("player or opponent already matched");
        }


        final long now = currentTimeMillis();

        final UpdateOperations<MongoMatch> playerUpdateOperations;
        playerUpdateOperations = getDatastore().createUpdateOperations(MongoMatch.class);

        final UpdateOperations<MongoMatch> oppponentUpdateOperations;
        oppponentUpdateOperations = getDatastore().createUpdateOperations(MongoMatch.class);

        playerUpdateOperations.set("opponent", opponentMatch).set("lastUpdatedTimestamp", now);
        oppponentUpdateOperations.set("opponent", playerMatch).set("lastUpdatedTimestamp", now);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .upsert(false)
            .returnNew(true);

        // Perform the updates as requested.  Both objects should exist, and if they don't then
        // we should throw an exception.  This is happening because something is deleting the
        // matches outside of the scope, which should never happen.

        final MongoMatch updatedPlayerMatch;
        updatedPlayerMatch = getDatastore().findAndModify(playerQuery, playerUpdateOperations, findAndModifyOptions);

        final MongoMatch updatedOpponentMatch;
        updatedOpponentMatch = getDatastore().findAndModify(opponentQuery, oppponentUpdateOperations, findAndModifyOptions);

        if (playerMatch == null || opponentMatch == null) {
            // This should never happen if the locking is done properly, so we chalk this up to an
            // internal error.
            throw new InternalException("player or opponent match was deleted while processing match");
        }

        final MongoMatchDelta playerDelta = deltaForUpdate(playerMatch);
        final MongoMatchDelta opponnentDelta = deltaForUpdate(opponentMatch);

        return new Matchmaker.SuccessfulMatchTuple() {

            @Override
            public Match getPlayerMatch() {
                return getDozerMapper().map(updatedPlayerMatch, Match.class);
            }

            @Override
            public Match getOpponentMatch() {
                return getDozerMapper().map(updatedOpponentMatch, Match.class);
            }

            @Override
            public List<MatchTimeDelta> getMatchDeltas() {
                return asList(
                    getDozerMapper().map(playerDelta, MatchTimeDelta.class),
                    getDozerMapper().map(opponnentDelta, MatchTimeDelta.class)
                );
            }

        };

    }

    private MongoMatchDelta deltaForUpdate(final MongoMatch mongoMatch) {
        try {
            return getMongoConcurrentUtils().performOptimisticInsert(ds -> {

                final MongoMatchDelta latestDelta = getLatestDelta(mongoMatch.getObjectId());

                if (latestDelta == null) {
                    // This can happen if the match is matched very close to when it's created
                    // and the latest delta hasn't even been inserted yet.
                    throw new MongoConcurrentUtils.ContentionException();
                }

                final MongoMatchDelta toInsert = new MongoMatchDelta();
                toInsert.setKey(latestDelta.getKey().nextInSequence(mongoMatch.getLastUpdatedTimestamp().getTime()));
                ds.insert(toInsert);

                return toInsert;

            });
        } catch (MongoConcurrentUtils.ConflictException e) {
            throw new TooBusyException(e);
        }
    }

    public MongoMatchDelta getLatestDelta(final String matchId) {
        final ObjectId objectId = getMongoDBUtils().parse(matchId);
        return getLatestDelta(objectId);
    }

    public MongoMatchDelta getLatestDelta(final ObjectId objectId) {
        final Query<MongoMatchDelta> matchTimeDeltaQuery = getDatastore().createQuery(MongoMatchDelta.class);
        matchTimeDeltaQuery.order(descending("sequence")).criteria("_id.match").equal(objectId);
        return matchTimeDeltaQuery.get();
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

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

}
