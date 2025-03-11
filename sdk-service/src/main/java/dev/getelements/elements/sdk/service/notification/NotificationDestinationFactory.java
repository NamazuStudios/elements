package dev.getelements.elements.sdk.service.notification;

import java.util.function.Function;
import java.util.stream.Stream;

@FunctionalInterface
public interface NotificationDestinationFactory extends Function<NotificationParameters, Stream<NotificationDestination>> {}
