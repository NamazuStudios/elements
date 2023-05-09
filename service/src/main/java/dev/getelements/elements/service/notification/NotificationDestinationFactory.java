package dev.getelements.elements.service.notification;

import dev.getelements.elements.service.NotificationParameters;

import java.util.function.Function;
import java.util.stream.Stream;

@FunctionalInterface
public interface NotificationDestinationFactory extends Function<NotificationParameters, Stream<NotificationDestination>> {}
