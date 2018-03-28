package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.exception.NotificationConfigurationException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.Notification;
import com.namazustudios.socialengine.service.NotificationBuilder;
import com.namazustudios.socialengine.service.NotificationParameters;

import javax.inject.Inject;
import java.util.function.Function;

public class StandardNotificationBuilder implements NotificationBuilder, NotificationParameters {

    private Profile recipient;

    private Application application;

    private String message;

    private Function<NotificationParameters, Notification> notificationFactory;

    @Override
    public NotificationBuilder withApplication(final Application application) {
        this.application = application;
        return this;
    }

    @Override
    public NotificationBuilder withRecipient(final Profile recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public NotificationBuilder withMessage(final String message) {
        this.message = message;
        return this;
    }

    @Override
    public Profile getRecipient() {
        return recipient;
    }

    @Override
    public Application getApplication() {
        return application;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Notification build() {
        return getNotificationFactory().apply(this);
    }

    public Function<NotificationParameters, Notification> getNotificationFactory() {
        return notificationFactory;
    }

    @Inject
    public void setNotificationFactory(Function<NotificationParameters, Notification> notificationFactory) {
        this.notificationFactory = notificationFactory;
    }

}
