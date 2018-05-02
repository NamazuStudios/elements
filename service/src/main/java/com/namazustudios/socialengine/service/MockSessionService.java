package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.session.MockSessionCreation;
import com.namazustudios.socialengine.model.session.MockSessionRequest;


public interface MockSessionService {

    MockSessionCreation createMockSession(MockSessionRequest mockSessionRequest);

}
