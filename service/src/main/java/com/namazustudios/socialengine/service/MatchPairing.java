package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.model.match.Match;

public interface MatchPairing {

    Match attempt(Matchmaker matchmaker, Match match);

}
