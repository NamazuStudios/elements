package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.match.Match;

public interface GameOnMatchDao {

    Match createMatch(String tournamentId, Match match);

    Matchmaker getMatchmaker(String tournamentId);

}
