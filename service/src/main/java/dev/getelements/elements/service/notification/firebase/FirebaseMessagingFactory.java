package dev.getelements.elements.service.notification.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.service.NotificationParameters;

import java.util.function.Function;

@FunctionalInterface
public interface FirebaseMessagingFactory extends Function<Application, FirebaseMessaging> {}
