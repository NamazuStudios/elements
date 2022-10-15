package com.namazustudios.socialengine.rt.annotation;

import com.namazustudios.socialengine.rt.exception.ServiceNotFoundException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Indicates a type is a remotely-invokable service. This may be done with or without proxying.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteService {

    /**
     * Names the remote service. If left blank, the name will be inferred.
     *
     * @return the name of the service
     */
    String value() default "";

    /**
     * Defines the various remote scope definitions.
     *
     * @return all {@link RemoteScope}s within this
     */
    RemoteScope[] scopes();

    final class Util {

        /**
         * Finds the name of a type annotated with {@link RemoteService}.
         *
         * @param cls the class
         * @return an {@link Optional<String>}
         */
        public static Optional<String> findName(final Class<?> cls) {
            return Optional
                .ofNullable(cls.getAnnotation(RemoteService.class))
                .map(m -> m.value().isBlank() ? cls.getName() : m.value());
        }

        /**
         * Finds the name of a type annotated with {@link RemoteService}.
         *
         * @param cls the class
         * @return an {@link Optional<String>}
         */
        public static String getName(final Class<?> cls) {
            return findName(cls).orElseThrow(() -> new ServiceNotFoundException(format("Service Not found for %s", cls)));
        }

        /**
         * Gets the definition matching scope and protocol.
         *
         * @param cls the type
         * @param protocol the protocol
         * @param scope the scope
         * @return the {@link Optional< RemoteScope >} which matches.
         */
        public static Optional<RemoteScope> findScope(
                final Class<?> cls,
                final String protocol,
                final String scope) {
            return Stream.of(cls.getAnnotationsByType(RemoteService.class))
                    .flatMap(rs -> Stream.of(rs.scopes()))
                    .filter(d -> scope.equals(d.scope()))
                    .filter(d -> protocol.equals(d.protocol()))
                    .findFirst();
        }

        /**
         * Gets the definition matching scope and protocol.
         *
         * @param cls the type
         * @param protocol the protocol
         * @param scope the scope
         * @return the {@link RemoteScope} which matches.
         */
        public static RemoteScope getScope(final Class<?> cls, final String protocol, final String scope) {
            return findScope(cls, protocol, scope)
                .orElseThrow(() -> new ServiceNotFoundException(format(
                    "Service Not found for %s (%s - %s)",
                    cls, scope, protocol
                )));
        }
    }

}
