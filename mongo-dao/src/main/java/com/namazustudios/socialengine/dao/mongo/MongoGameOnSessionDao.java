package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.GameOnSessionDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.gameon.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.GameOnSession;
import com.namazustudios.socialengine.model.profile.Profile;

public class MongoGameOnSessionDao implements GameOnSessionDao {

    // TODO

    @Override
    public Pagination<GameOnSession> getSessionsForUser(final User user, final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<GameOnSession> getSessionsForUser(final User user, final int offset, final int count, final String search) {
        return null;
    }

    @Override
    public GameOnSession getSessionForUser(final User user, final String gameOnSessionId) {
        return null;
    }

    @Override
    public GameOnSession getSessionForProfile(final Profile profile, final DeviceOSType deviceOSType) {
        return null;
    }

    @Override
    public void deleteSessionForUser(final User user, final String gameOnSessionId) {

    }

    @Override
    public GameOnSession createSession(final GameOnSession authenticated) {
        return null;
    }

}
