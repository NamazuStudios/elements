package dev.getelements.elements.sdk.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassLoaderUtils {

    private final Logger logger;

    public ClassLoaderUtils(final Class<? extends ClassLoader> classLoaderClass) {
        logger = LoggerFactory.getLogger(classLoaderClass);
    }

    public boolean isAnnotatedWithSdkAnnotation(final Class<?> aClass) {

        if (aClass == null) {
            logger.warn("Class {} is null", aClass);
            return false;
        }

        boolean result = false;

        for (final var annotation : aClass.getAnnotations())    {
            if (annotation.annotationType().getPackageName().startsWith("dev.getelements.elements.sdk")) {
                result = true;
            }
        }

        if (result) {
            for (final var annotation : aClass.getAnnotations()) {
                if (annotation.annotationType().getPackageName().startsWith("dev.getelements.elements.sdk")) {
                    logger.trace("Class {} is annotated with SDK annotation: {}", aClass.getName(), annotation);
                }
            }
        } else {

            logger.warn("Class {} is not annotated with SDK annotation.", aClass.getName());

            for (final var annotation : aClass.getAnnotations()) {
                if (annotation.annotationType().getPackageName().startsWith("dev.getelements.elements.sdk")) {
                    logger.warn(" -> Class {} has: {}", aClass.getName(), annotation);
                }
            }

        }

        return result;

    }

}
