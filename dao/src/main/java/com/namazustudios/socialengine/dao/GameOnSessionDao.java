package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.gameon.game.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Expose;

import static com.namazustudios.socialengine.model.gameon.game.DeviceOSType.valueOf;

@Expose(modules = "namazu.elements.dao.gameon.session")
public interface GameOnSessionDao {

    /**
     * Gets all {@link GameOnSession} instances for the supplied {@link User}.
     *
     * @param user the {@link User} to filter by
     * @param offset the offset in the dataset
     * @param count the number of results to return from the data set
     * @return the {@link Pagination<GameOnSession>}
     */
    Pagination<GameOnSession> getSessionsForUser(User user, int offset, int count);

    /**
     * Gets all {@link GameOnSession} instances for the supplied {@link User} specifying a search query.
     *
     * @param user the {@link User} to filter by
     * @param offset the offset in the dataset
     * @param count the number of results to return from the data set
     * @return the {@link Pagination<GameOnSession>}
     */
    Pagination<GameOnSession> getSessionsForUser(User user, int offset, int count, String search);

    /**
     * Gets a specific {@link GameOnSession} specifying the {@link User} as the identifying criteria.
     *
     * @param user the {@link User}
     * @param gameOnSessionId the session ID determined b {@link GameOnSession#getId()}
     * @return the {@link GameOnSession}, never null
     */
    GameOnSession getSessionForUser(User user, String gameOnSessionId);

    /**
     * Gets the signle designated {@link GameOnSession} for the supplied {@link Profile}.
     *
     * @param profile the {@link Profile}
     * @param deviceOSType
     * @return the {@link GameOnSession}, never null
     */
    default GameOnSession getSessionForProfile(Profile profile, String deviceOSType) {
        return getSessionForProfile(profile, DeviceOSType.valueOf(deviceOSType));
    }

    /**
     * Gets the signle designated {@link GameOnSession} for the supplied {@link Profile}.
     *
     * @param profile the {@link Profile}
     * @param deviceOSType
     * @return the {@link GameOnSession}, never null
     */
    GameOnSession getSessionForProfile(Profile profile, DeviceOSType deviceOSType);

    /**
     * Deletes the supplied {@link GameOnSession} for for the user and game on session id.
     *
     * @param user the {@link User} who owns the {@link GameOnSession}
     * @param gameOnSessionId the value of {@link GameOnSession#getId()}
     */
    void deleteSessionForUser(User user, String gameOnSessionId);

    /**
     * Deletes a {@link GameOnSession} with the specified ID
     *
     * @param id the id as reported by {@link GameOnSession#getId()}
     */
    void deleteSession(String id);

    /**
     * Creates the {@link GameOnSession} in the database.  The {@link GameOnSession} must have previously been
     * created by calls to the Amazon Game On APIs.  This merely stores it in the database.  The stored session may take
     * into account the supplied expiration when querying sessions.
     *
     * @param authenticated the {@link GameOnSession} that has been authenticated against amazon game on.
     * @return the {@link GameOnSession}
     */
    GameOnSession createSession(GameOnSession authenticated);

}
