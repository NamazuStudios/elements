package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.Notification;
import com.namazustudios.socialengine.service.NotificationBuilder;
import com.namazustudios.socialengine.service.NotificationParameters;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StandardNotificationBuilder implements NotificationBuilder, NotificationParameters {

    private Profile recipient;

    private Application application;

    private String title;

    private String message;

    private NotificationFactory notificationFactory;

    private String sound;

    private HashMap<String, String> extraProperties = new HashMap<>();

    @Override
    public NotificationBuilder application(final Application application) {
        this.application = application;
        return this;
    }

    @Override
    public NotificationBuilder recipient(final Profile recipient) {
        this.recipient = recipient;
        return this;
    }

    @Override
    public NotificationBuilder title(final String title) {
        this.title = title;
        return this;
    }

    @Override
    public NotificationBuilder message(final String message) {
        this.message = message;
        return this;
    }

    @Override
    public NotificationBuilder sound(final String sound) {
        this.sound = sound;
        return this;
    }

    @Override
    public NotificationBuilder add(@Nonnull String key, @Nonnull String value) {
        extraProperties.put(key, value);
        return this;
    }

    @Override
    public NotificationBuilder addAll(Map<String, String> allData) {
        if (allData == null) {
            return this;
        }
        for (Map.Entry<String, String> kv : allData.entrySet()) {
            if (!StringUtils.isNotEmpty(kv.getKey()) && kv.getValue() != null) {
                extraProperties.put(kv.getKey(), kv.getValue());
            }
        }
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
    public String getTitle() {
        return title;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getSound() {
        return sound;
    }

    @Override
    @Nonnull
    public Map<String, String> getExtraProperties() {
        return Collections.unmodifiableMap(extraProperties);
    }

    @Override
    public Notification build() {
        return getNotificationFactory().apply(this);
    }

    public Function<NotificationParameters, Notification> getNotificationFactory() {
        return notificationFactory;
    }

    @Inject
    @SuppressWarnings("unused")
    public void setNotificationFactory(NotificationFactory notificationFactory) {
        this.notificationFactory = notificationFactory;
    }

    @Override
    public String toString() {
        return "StandardNotificationBuilder{" +
               "recipient=" + recipient +
               ", application=" + application +
               ", title='" + title + '\'' +
               ", message='" + message + '\'' +
               ", notificationFactory=" + notificationFactory +
               '}';
    }

}
