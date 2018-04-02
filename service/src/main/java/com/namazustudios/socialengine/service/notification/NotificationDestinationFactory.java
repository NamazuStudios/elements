package com.namazustudios.socialengine.service.notification;

import com.namazustudios.socialengine.service.NotificationParameters;

import java.util.function.Function;
import java.util.stream.Stream;

@FunctionalInterface
public interface NotificationDestinationFactory extends Function<NotificationParameters, Stream<NotificationDestination>> {}
