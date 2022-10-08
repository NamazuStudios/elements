package com.namazustudios.socialengine.rt.annotation;

import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
import com.namazustudios.socialengine.rt.exception.ParameterNotFoundException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.Optional;

import static com.namazustudios.socialengine.rt.annotation.CodeStyle.JVM_NATIVE;

/**
 * Designates a method parameters to be serialized for remote invocation.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Serialize {

    /**
     * The name of the parameter. If left a blank string, then the name will be inferred by the
     *
     * @return the name of the parameter
     */
    String value() default "";

    class Util {
        private Util() {}

        /**
         * Finds the raw name of the supplied {@link Parameter}. This is before applying any changes to the \
         * {@link CodeStyle}.
         *
         * @param parameter the parameter
         * @return an {@link Optional<String>} of the raw name
         */
        public static Optional<String> findRawName(final Parameter parameter) {

            final var serialize = parameter.getAnnotation(Serialize.class);

            if (serialize == null || serialize.value().isBlank()) {
                return parameter.isNamePresent() ? Optional.of(parameter.getName()) : Optional.empty();
            } else {
                return Optional.of(serialize.value().trim());
            }

        }

        /**
         * Accepting the supplied {@link CodeStyle}, this converts the raw parameter name to the supplied
         * {@link CodeStyle}.
         *
         * @param parameter the parameter
         * @param codeStyle the {@link CodeStyle}
         * @return an {@link Optional<String>} of the name
         */
        public static Optional<String> findName(final Parameter parameter, final CodeStyle codeStyle) {
            final var parameterCaseFormat = codeStyle.parameterCaseFormat();
            return findRawName(parameter).map(name -> JVM_NATIVE.parameterCaseFormat().to(parameterCaseFormat, name));
        }

        /**
         * Accepting the supplied {@link CodeStyle}, this converts the raw parameter name to the supplied
         * {@link CodeStyle}.
         *
         * @param parameter the parameter
         * @param codeStyle the {@link CodeStyle}
         * @return an {@link Optional<String>} of the name
         */
        public static String getName(final Parameter parameter, final CodeStyle codeStyle) {
            return findName(parameter, codeStyle).orElseThrow(() -> new ParameterNotFoundException(
                "No name for parameter: " + parameter.getName()
            ));
        }

    }

}
