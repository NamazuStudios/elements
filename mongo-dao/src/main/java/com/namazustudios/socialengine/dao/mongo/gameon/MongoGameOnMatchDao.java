package com.namazustudios.socialengine.dao.mongo.gameon;

import com.namazustudios.socialengine.dao.GameOnMatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.model.match.Match;

public class MongoGameOnMatchDao implements GameOnMatchDao {

    @Override
    public Matchmaker getMatchmaker(final String tournamentId) {
        return null;
    }

    @Override
    public Match createMatch(final String tournamentId, final Match match) {
        return null;
    }

}
