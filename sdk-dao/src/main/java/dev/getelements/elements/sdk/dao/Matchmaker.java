package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.NoSuitableMatchException;
import dev.getelements.elements.sdk.model.match.Match;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 7/27/17.
 */
public interface Matchmaker {

    int DEFAULT_MAX_CANDIDATES = 100;

    /**
     * Invokes {@link #attemptToFindOpponent(Match, int, BiFunction<Match, Match, String>)} using the {@link #DEFAULT_MAX_CANDIDATES} value.
     *
     * @param match the {@link Match} to use
     * @return a {@link SuccessfulMatchTuple} representing a successful match, never null
     * @throws NoSuitableMatchException if there is no suitable match found
     */
    default SuccessfulMatchTuple attemptToFindOpponent(
            final Match match,
            final BiFunction<Match, Match, String> finalizer) throws NoSuitableMatchException {
        return attemptToFindOpponent(match, DEFAULT_MAX_CANDIDATES, finalizer);
    }

    /**
     * Attempts to find an opponent for the supplied {@link Match} instance.  This will query the database for suitable
     * matches.  This will return a {@link SuccessfulMatchTuple} combining the match of the player and the opponent's
     * match.
     * <p>
     * Thia also finalizes the matching process by flagging the {@link Match} instances for deletion and invoking the
     * finalizer {@link Supplier<String>}.  The supplied {@link Supplier<String>} returns a system-wide unique ID
     * used to process to identify the game that was created as the result of the {@link Match}.  The return value
     * of this method will be assigned to the match using {@link Match#setGameId(String)}.
     * <p>
     * Note that this method guarantees that the supplied {@link Supplier<String>} finalizer will only ever be
     * called once per successful matching tuple as multiple players may attempt to finalize the pairing at the same
     * time.  The return value indicates the affected {@link Match} instances, or returns an emnpty stream if no
     * {@link Match} instances were affected by the finalization.
     * <p>
     * Because both players may not have read the {@link Match}, the involved {@link Match} instances will be marked
     * for timeout and deletion at a later time.
     *
     * @param match                   the {@link Match} the player match
     * @param maxCandidatesToConsider the maximum number of candidates to consider
     * @param finalizer               the {@link Supplier} used to finalize the match and provide the resulting game id
     * @return a {@link SuccessfulMatchTuple} representing a successful match, never null
     * @throws NoSuitableMatchException if there is no suitable match found
     */
    SuccessfulMatchTuple attemptToFindOpponent(
            Match match, int maxCandidatesToConsider,
            BiFunction<Match, Match, String> finalizer) throws NoSuitableMatchException;

    /**
     * Restricts this {@link Matchmaker} to the supplied scope.  Will return only whose {@link Match#getScope()} method
     * matches the supplied scope.  Unscoped {@link Match} instances will also be excluded from the candidate pool.
     *
     * @param scope the scope
     * @return this instance
     */
    Matchmaker withScope(String scope);

    /**
     * Combines a {@link Match} for both a player and an opponent.  This is used to supply
     * information on a succssful match between players only.
     */
    interface SuccessfulMatchTuple {

        /**
         * The opponent {@link Match}.
         *
         * @return the player match
         */
        Match getPlayerMatch();

        /**
         * The opponent {@link Match}.
         *
         * @return
         */
        Match getOpponentMatch();

    }

}
