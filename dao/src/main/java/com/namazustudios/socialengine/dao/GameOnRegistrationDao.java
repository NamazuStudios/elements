package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.model.profile.Profile;

public interface GameOnRegistrationDao {

    GameOnRegistration getRegistrationForProfile(Profile profile);

    GameOnRegistration getRegistrationForProfile(Profile profile, String gameOnRegistrationId);

    Pagination<GameOnRegistration> getRegistrationsForUser(User user, int offset, int count);

    Pagination<GameOnRegistration> getRegistrationsForUser(User user, int offset, int count, String search);

    GameOnRegistration createRegistration(GameOnRegistration gameOnRegistration);

    void deleteRegistration(Profile profile, String gameOnRegistrationId);

}
