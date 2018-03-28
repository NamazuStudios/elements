package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.service.Notification;
import com.namazustudios.socialengine.service.NotificationParameters;

import java.util.function.Consumer;

public class StandardNotification implements Notification {

    private final NotificationParameters notificationParameters;

    public StandardNotification(NotificationParameters notificationParameters) {
        this.notificationParameters = notificationParameters;
    }

    @Override
    public void send(final Consumer<Void> success, final Consumer<Exception> failure) {

    }

    public NotificationParameters getNotificationParameters() {
        return notificationParameters;
    }

}
