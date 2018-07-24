package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.gameon.GameOnSession;

public interface GameOnSessionService {
    
    Pagination<GameOnSession> getGameOnSessions(int offset, int count);

    Pagination<GameOnSession> getGameOnSessions(int offset, int count, String search);

    GameOnSession getGameOnSession(String gameOnSessionId);

    GameOnSession getCurrentGameOnSession();

    GameOnSession createSession(GameOnSession gameOnSession);

    void deleteSession(String gameOnSessionId);

}
