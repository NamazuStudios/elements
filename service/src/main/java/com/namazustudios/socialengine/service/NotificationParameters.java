package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;

public interface NotificationParameters {

    Profile getRecipient();

    Application getApplication();

    String getMessage();

}
