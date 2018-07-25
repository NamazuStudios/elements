package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.gameon.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.GameOnSession;
import com.namazustudios.socialengine.model.profile.Profile;

public interface GameOnSessionService {

    /**
     * Gets a {@link Pagination<GameOnSession>} instance.
     *
     * @param offset the offset in the data set
     * @param count the number of results to return per page
     *
     * @return the {@link Pagination<GameOnSession>}, never null
     */
    Pagination<GameOnSession> getGameOnSessions(int offset, int count);

    /**
     * Gets a {@link Pagination<GameOnSession>} instance, filtered by the supplied search query.
     *
     * @param offset the offset in the data set
     * @param count the number of results to return per page
     *
     * @return the {@link Pagination<GameOnSession>}, never null
     */
    Pagination<GameOnSession> getGameOnSessions(int offset, int count, String search);

    /**
     * Gets the {@link GameOnSession} with the supplied id.
     *
     * @param gameOnSessionId the id of the as provided by {@link GameOnSession#getId()}
     * @return the {@link GameOnSession}, never null
     */
    GameOnSession getGameOnSession(String gameOnSessionId);

    /**
     * Gets a {@link GameOnSession} for the currently logged-in {@link Profile} filtered by the supplied
     * {@link DeviceOSType}.
     *
     * @param deviceOSType the {@link DeviceOSType}
     * @return the {@link GameOnSession}, never null
     */
    GameOnSession getCurrentGameOnSession(DeviceOSType deviceOSType);

    /**
     * Creates an intance of {@link GameOnSession} by first registring the {@link GameOnSession} with Amazon and then
     * stores the resulting instance in the database.
     *
     * @param gameOnSession the {@link GameOnSession}
     * @return the {@link GameOnSession} as recorded by the service
     */
    GameOnSession createSession(GameOnSession gameOnSession);

    /**
     * Deltes the {@link GameOnSession} with the supplied id as determined by {@link GameOnSession#getId()}
     *
     * @param gameOnSessionId
     */
    void deleteSession(String gameOnSessionId);

}
