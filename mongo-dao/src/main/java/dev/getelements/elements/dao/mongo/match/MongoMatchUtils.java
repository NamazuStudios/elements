package dev.getelements.elements.dao.mongo.match;

import dev.getelements.elements.sdk.dao.Matchmaker;
import dev.getelements.elements.dao.mongo.MongoConcurrentUtils;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.match.MongoMatch;
import dev.getelements.elements.dao.mongo.model.match.MongoMatchLock;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.NoSuitableMatchException;
import dev.getelements.elements.sdk.model.match.Match;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Used to actually perform the match between two players.  This ensures that two matches are made
 * with consistency and all appropriate diffs are created.
 *
 * Created by patricktwohig on 7/27/17.
 */
public class MongoMatchUtils {

    private static final Logger logger = LoggerFactory.getLogger(MongoMatchUtils.class);

    private Datastore datastore;

    private MapperRegistry dozerMapperRegistry;

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

        // TODO Define proper constant or static method to determine the lock timestamp
        final Timestamp timeout = new Timestamp(currentTimeMillis() - 5000);

        final List<MatchLockContext> contexts = stream(mongoMatches)
            .map(m -> new MatchLockContext(m, timeout))
            .collect(toList());

        try {
            for (final MatchLockContext c : contexts) c.attempt();
            return resultProvider.get();
        } finally {
            for (final MatchLockContext c : contexts) c.release();
        }

    }

    public Matchmaker.SuccessfulMatchTuple attemptToPairCandidates(
            final MongoMatch playerMatch,
            final List<MongoMatch> candidatesList,
            final BiFunction<Match, Match, String> finalizer) throws NoSuitableMatchException {

        for (final MongoMatch candidateMatch : candidatesList) {

            if (Objects.equals(playerMatch.getObjectId(), candidateMatch.getObjectId())) {
                continue;
            }

            final MongoMatch refreshed = getDatastore().find(MongoMatch.class).filter(eq("_id", playerMatch.getObjectId())).first();

            if (refreshed.getPlayer() != null && refreshed.getGameId() != null) {

                final MongoMatch other = getDatastore().find(MongoMatch.class)
                      .filter(Filters.and(
                              eq("player", refreshed.getOpponent()),
                              eq("opponent", refreshed.getPlayer()),
                              eq("gameId", refreshed.getGameId())
                      )).first();

                if (other != null) {
                    return new Matchmaker.SuccessfulMatchTuple() {
                        @Override
                        public Match getPlayerMatch() {
                            return getDozerMapper().map(refreshed, Match.class);
                        }

                        @Override
                        public Match getOpponentMatch() {
                            return getDozerMapper().map(other, Match.class);
                        }
                    };
                }

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
            .find(MongoMatch.class)
            .filter(eq("_id", playerMatch.getObjectId()));

        final Query<MongoMatch> opponentQuery = getDatastore()
            .find(MongoMatch.class)
            .filter(eq("_id", opponentMatch.getObjectId()));

        playerMatch = playerQuery.first();
        opponentMatch = opponentQuery.first();

        if (playerMatch == null || opponentMatch == null) {
            throw new NoSuitableMatchException("player or opponent not found");
        } else if (playerMatch.getOpponent() != null || opponentMatch.getOpponent() != null) {
            throw new NoSuitableMatchException("player or opponent already matched");
        }

        playerMatch.setOpponent(opponentMatch.getPlayer());
        opponentMatch.setOpponent(playerMatch.getPlayer());

        final String gameId = finalizer.apply(
            getDozerMapper().map(playerMatch, Match.class),
            getDozerMapper().map(opponentMatch, Match.class)
        );

        final var now = new Timestamp(currentTimeMillis());

        if (gameId == null) {
            throw new InternalException("Unspecified gameId");
        }

        // Perform the updates as requested.  Both objects should exist, and if they don't then
        // we should throw an exception.  This is happening because something is deleting the
        // matches outside of the scope, which should never happen.

        final var updatedPlayerMatch = playerQuery.modify(
            set("opponent", opponentMatch.getPlayer()),
            set("expiry", now),
            set("lastUpdatedTimestamp", now),
            set("gameId", gameId)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

        final var updatedOpponentMatch = opponentQuery.modify(
            set("opponent", playerMatch.getPlayer()),
            set("expiry", now),
            set("lastUpdatedTimestamp", now),
            set("gameId", gameId)
        ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

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

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    private class MatchLockContext {

        private final MongoMatch match;

        private final Timestamp timeout;

        private final MongoMatchLock lock = new MongoMatchLock();

        public MatchLockContext(final MongoMatch match, final Timestamp timeout) {
            this.match = match;
            this.timeout = timeout;
        }

        public void attempt() throws MongoConcurrentUtils.ContentionException {

            final var query = getDatastore().find(MongoMatch.class);

            query.filter(eq("_id", match.getObjectId()))
                 .filter(or(
                        exists("lock").not(),
                        lt("lock.timestamp", timeout)
                     )
                 );

            final var result = query.modify(
                set("lock", lock)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER));

            if (result == null) {
                throw new MongoConcurrentUtils.ContentionException();
            }

        }

        public void release() {

            final var query = getDatastore().find(MongoMatch.class);

            query.filter(eq("_id", match.getObjectId()))
                 .filter(eq("lock.uuid", lock.getUuid()));

            query.update(
                unset("lock")
            ).execute(new UpdateOptions().upsert(false));

        }

    }

}
