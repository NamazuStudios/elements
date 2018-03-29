package com.namazustudios.socialengine.service.notification.guice;

import com.google.inject.assistedinject.Assisted;
import com.namazustudios.socialengine.service.NotificationParameters;
import com.namazustudios.socialengine.service.notification.StandardNotification;

public class GuiceStandardNotification extends StandardNotification {

    public GuiceStandardNotification(@Assisted final NotificationParameters notificationParameters) {
        super(notificationParameters);
    }

}
