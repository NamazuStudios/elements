package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.mongo.model.MongoMatch;
import com.namazustudios.socialengine.dao.mongo.model.MongoMatchLock;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.model.match.Match;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;

/**
 * Used to actually perform the match between two players.  This ensures that two matches are made
 * with consistency and all appropriate diffs are created.
 *
 * Created by patricktwohig on 7/27/17.
 */
public class MongoMatchUtils {

    private static final Logger logger = LoggerFactory.getLogger(MongoMatchUtils.class);

    private AdvancedDatastore datastore;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    /**
     * Intended to be used in the scope of
     * {@link MongoConcurrentUtils#performOptimistic(MongoConcurrentUtils.CriticalOperation)},
     * this will attempt to lock the {@link MongoMatch} using a unique {@link MongoMatchLock}.  If successful, this will
     * perform the operation defined by the supplied {@link Provider<T>} instance.  If the lock fails, then this will
     * throw an instance of {@link MongoConcurrentUtils.ContentionException} to indicate that a re-lock must be
     * attempted or the process abandoned by the calling code.
     *
     * This is necessary because matching may happen at any time which involves controlling access to multiple
     * documents.  Therefore code which mutates or deletes a {@link MongoMatch} should only be done within the context
     * of a lock.  This allows for acquiring multiple {@link MongoMatchLock} instances and safely unlocking them even if
     * the code specified in the {@link Provider<T> }fails to execute.
     *
     * @param resultProvider the {@link Provider<T>} defining the operation to perform
     * @param mongoMatches one or {@link MongoMatch} instances to return
     * @param <T> the return type
     * @return the value returned by the supplied provider
     * @throws MongoConcurrentUtils.ContentionException if locking fails
     */
    public <T> T attemptLock(
            final Provider<T> resultProvider,
            final MongoMatch ... mongoMatches) throws MongoConcurrentUtils.ContentionException {

        final List<MongoMatchLock> matchLockList = stream(mongoMatches)
            .map(m -> new MongoMatchLock(m.getObjectId()))
            .collect(Collectors.toList());

        return attemptLock(resultProvider, matchLockList);

    }

    /**
     * Intended to be used in the scope of
     * {@link MongoConcurrentUtils#performOptimistic(MongoConcurrentUtils.CriticalOperation)},
     * this will attempt to lock the {@link MongoMatch} using a unique {@link MongoMatchLock}.  If successful, this will
     * perform the operation defined by the supplied {@link Provider<T>} instance.  If the lock fails, then this will
     * throw an instance of {@link MongoConcurrentUtils.ContentionException} to indicate that a re-lock must be
     * attempted or the process abandoned by the calling code.
     *
     * This is necessary because matching may happen at any time which involves controlling access to multiple
     * documents.  Therefore code which mutates or deletes a {@link MongoMatch} should only be done within the context
     * of a lock.  This allows for acquiring multiple {@link MongoMatchLock} instances and safely unlocking them even if
     * the code specified in the {@link Provider<T> }fails to execute.
     *
     * @param resultProvider the {@link Provider<T>} defining the operation to perform
     * @param mongoMatchIds one or {@link MongoMatch} instances to return
     * @param <T> the return type
     * @return the value returned by the supplied provider
     * @throws MongoConcurrentUtils.ContentionException if locking fails
     */
    public <T> T attemptLock(
            final Provider<T> resultProvider,
            final ObjectId ... mongoMatchIds) throws MongoConcurrentUtils.ContentionException {

        final List<MongoMatchLock> matchLockList = stream(mongoMatchIds)
                .map(id -> new MongoMatchLock(id))
                .collect(Collectors.toList());

        return attemptLock(resultProvider, matchLockList);

    }

    private <T> T attemptLock(
            final Provider<T> resultProvider,
            final List<MongoMatchLock> matchLockList) throws MongoConcurrentUtils.ContentionException {
        try {
            getDatastore().insert(matchLockList);
            return resultProvider.get();
        } catch (MongoException ex) {

            if (ex.getCode() == 11000) {

                // The only expected exception here is the duplicate key exception, which happens if the lock is is
                // currently acquired by another thread.  In the case of any exception, the finally block attached to
                // this block will ensure that only the locks we have acquired will be released.

                throw new MongoConcurrentUtils.ContentionException(ex);

            } else {
                throw new InternalException(ex);
            }

        } finally {
            unlock(matchLockList);
        }
    }

    private void unlock(final List<MongoMatchLock> mongoMatchLockList) {
        mongoMatchLockList.forEach(lock -> {
            try {

                final Query<MongoMatchLock> qbe = getDatastore()
                    .createQuery(MongoMatchLock.class)
                    .field("_id").equal(lock.getPlayerMatchId())
                    .field("lockUuid").equal(lock.getLockUuid());

                final WriteResult writeResult = getDatastore().delete(qbe);

                if (writeResult.getN() > 1) {
                    logger.error("Unexpected delete count for lock {}.  Expected 1.  Got {}", lock, writeResult.getN());
                }

            } catch (Exception ex) {
                // Just in case we try to unlock an object we didn't create we must ensure that we at least attempt to
                // unlock the other locks.
                logger.warn("Failed to unlock match {} ", lock.getPlayerMatchId(), ex);
            }

        });
    }

    public Matchmaker.SuccessfulMatchTuple attemptToPairCandidates(
            final MongoMatch playerMatch,
            final List<MongoMatch> candidatesList,
            final BiFunction<Match, Match, String> finalizer) throws NoSuitableMatchException {

        for (final MongoMatch candidateMatch : candidatesList) {

            if (Objects.equals(playerMatch.getObjectId(), candidateMatch.getObjectId())) {
                continue;
            }

            try {
                return attemptToPairCandidates(playerMatch, candidateMatch, finalizer);
            } catch (NoSuitableMatchException ex) {
                // We keep attempting until we have exhausted all possible options
                // in the supplied list.
                continue;
            }

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
            final MongoMatch opponentMatch,
            final BiFunction<Match, Match, String> finalizer) throws NoSuitableMatchException {

        try {
            return attemptLock(() -> doAttempt(playerMatch, opponentMatch, finalizer), playerMatch, opponentMatch);
        } catch (MongoConcurrentUtils.ContentionException ex) {
            // Failing to acquire a lock is a very good reason to say there's no suitable match.  So we simply skip this
            // attempt and provide the exception.
            throw new NoSuitableMatchException(ex);
        }

    }

    private Matchmaker.SuccessfulMatchTuple doAttempt(MongoMatch playerMatch,
                                                      MongoMatch opponentMatch,
                                                      BiFunction<Match, Match, String> finalizer) throws NoSuitableMatchException {

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

        playerMatch.setOpponent(opponentMatch.getPlayer());
        opponentMatch.setOpponent(playerMatch.getPlayer());

        final String gameId = finalizer.apply(
            getDozerMapper().map(playerMatch, Match.class),
            getDozerMapper().map(opponentMatch, Match.class));

        final Timestamp now = new Timestamp(currentTimeMillis());

        final UpdateOperations<MongoMatch> playerUpdateOperations;
        playerUpdateOperations = getDatastore().createUpdateOperations(MongoMatch.class);

        final UpdateOperations<MongoMatch> oppponentUpdateOperations;
        oppponentUpdateOperations = getDatastore().createUpdateOperations(MongoMatch.class);

        playerUpdateOperations
            .set("opponent", opponentMatch.getPlayer())
            .set("expiry", now)
            .set("lastUpdatedTimestamp", now)
            .set("gameId", gameId);

        oppponentUpdateOperations
            .set("opponent", playerMatch.getPlayer())
            .set("expiry", now)
            .set("lastUpdatedTimestamp", now)
            .set("gameId", gameId);

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

        return new Matchmaker.SuccessfulMatchTuple() {

            @Override
            public Match getPlayerMatch() {
                return getDozerMapper().map(updatedPlayerMatch, Match.class);
            }

            @Override
            public Match getOpponentMatch() {
                return getDozerMapper().map(updatedOpponentMatch, Match.class);
            }

        };

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

}
