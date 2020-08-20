package com.namazustudios.socialengine.dao.mongo.applesignin;

import com.namazustudios.socialengine.dao.AppleSignInSessionDao;
import com.namazustudios.socialengine.model.applesignin.TokenResponse;
import com.namazustudios.socialengine.model.session.AppleSignInSession;
import com.namazustudios.socialengine.model.session.AppleSignInSessionCreation;
import com.namazustudios.socialengine.model.session.Session;

import java.util.Optional;

public class MongoAppleSignInSessionDao implements AppleSignInSessionDao {

    @Override
    public AppleSignInSessionCreation create(final Session session, final TokenResponse tokenResponse) {
        return null;
    }

    @Override
    public Optional<AppleSignInSession> findSession(String sessionSecret) {
        return Optional.empty();
    }

}
