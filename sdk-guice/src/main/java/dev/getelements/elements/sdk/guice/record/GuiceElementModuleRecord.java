package dev.getelements.elements.sdk.guice.record;

import com.google.inject.Module;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.guice.annotations.GuiceElementModule;
import dev.getelements.elements.sdk.guice.annotations.GuiceElementModules;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * A record type for {@link GuiceElementModule}.
 * @param moduleType the type of {@link Module} to import.
 */
public record GuiceElementModuleRecord(Class<? extends Module> moduleType) {

    /**
     * Creates an instance of the {@link  Module} type.
     *
     * @return the new {@link Module} instance
     */
    public Module newModule() {
        try {
            return moduleType()
                    .getConstructor()
                    .newInstance();
        } catch (InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException |
                 NoSuchMethodException e) {
            throw new SdkException(e);
        }
    }

    /**
     * Creates a {@link Stream} of {@link GuiceElementModuleRecord}s from the supplied {@link Package}
     * @param aPackage the {@link Package} with the {@link GuiceElementModule} annotation
     * @return a {@link Stream} of records
     */
    public static Stream<GuiceElementModuleRecord> fromPackage(final Package aPackage) {

        var modules = aPackage.getAnnotation(GuiceElementModules.class);
        var annotations = Stream.of(aPackage.getAnnotationsByType(GuiceElementModule.class));

        if (modules != null) {
            annotations = Stream.concat(annotations, Stream.of(modules.value()));
        }

        return annotations.map(GuiceElementModuleRecord::from);

    }

    /**
     * Creates a {@link GuiceElementModuleRecord} from the supplied {@link GuiceElementModule} annotation.
     *
     * @param guiceElementModule the {@link GuiceElementModule} annotation
     * @return the {@link GuiceElementModuleRecord}
     */
    public static GuiceElementModuleRecord from(GuiceElementModule guiceElementModule) {
        requireNonNull(guiceElementModule, "guiceElementModule");
        return new GuiceElementModuleRecord(guiceElementModule.value());
    }

}
