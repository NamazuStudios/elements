package dev.getelements.elements.rt.annotation;

import dev.getelements.elements.rt.exception.ModelNotFoundException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteModel {

    /**
     * Names the remote model. If left blank, the name will be inferred.
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
         * Finds the name of a type annotated with {@link RemoteModel}.
         *
         * @param cls the class
         * @return an {@link Optional<String>}
         */
        public static Optional<String> findName(final Class<?> cls) {
            return Optional
                .ofNullable(cls.getAnnotation(RemoteModel.class))
                .map(m -> m.value().isBlank() ? cls.getSimpleName() : m.value());
        }

        /**
         * Finds the name of a type annotated with {@link RemoteModel}.
         *
         * @param cls the class
         * @return an {@link Optional<String>}
         */
        public static String getName(final Class<?> cls) {
            return findName(cls).orElseThrow(() -> new ModelNotFoundException(format("Model Not found for %s", cls)));
        }

        /**
         * Gets the definition matching scope and protocol.
         *
         * @param cls the type
         * @param protocol the protocol
         * @param scope the scope
         * @return the {@link Optional < RemoteScope >} which matches.
         */
        public static Optional<RemoteScope> findScope(
                final Class<?> cls,
                final String protocol,
                final String scope) {
            return Stream.of(cls.getAnnotationsByType(RemoteModel.class))
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
                .orElseThrow(() -> new ModelNotFoundException(format(
                        "Model Not found for %s (%s - %s)",
                        cls, scope, protocol
                )));
        }
    }

}
