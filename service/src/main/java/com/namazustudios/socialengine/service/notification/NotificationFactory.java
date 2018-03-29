package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.service.Notification;
import com.namazustudios.socialengine.service.NotificationParameters;

import java.util.function.Function;

public interface NotificationFactory extends Function<NotificationParameters, Notification> {}
