package com.namazustudios.socialengine.service.notification.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.NotificationParameters;

import java.util.function.Function;

@FunctionalInterface
public interface FirebaseMessagingFactory extends Function<Application, FirebaseMessaging> {}
