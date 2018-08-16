package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import com.namazustudios.socialengine.model.match.Match;

/**
 * A type that assists the services classes in paring players.   This performs the action of attempting to pair two
 * players and then signal other players who may be waiting for a response.
 */
public interface MatchServiceUtils {

    /**
     * Attempts to create a pairing between two players, using the supplied {@link Matchmaker}
     *
     * @param matchmaker the {@link Matchmaker} to use
     * @param match the originating player match.
     * @param configuration the {@link MatchmakingApplicationConfiguration} to use when finalizing the match
     * @return the {@link Match} after the attempt has been made to pair it.
     */
    Match attempt(Matchmaker matchmaker, Match match, MatchmakingApplicationConfiguration configuration);

    /**
     * Redacts any private information for the opponent user so an opponent may not see the personal details of
     * an opponent {@link com.namazustudios.socialengine.model.User}.
     *
     * @param match the {@link Match} info
     * @return the same {@link Match} with private information redacted
     */
    Match redactOpponentUser(Match match);

}
