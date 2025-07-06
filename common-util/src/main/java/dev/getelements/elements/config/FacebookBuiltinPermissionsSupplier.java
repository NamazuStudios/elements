package dev.getelements.elements.config;

import dev.getelements.elements.sdk.model.annotation.FacebookPermission;
import dev.getelements.elements.sdk.model.annotation.FacebookPermissions;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Scans for {@link FacebookPermission} annotated types.
 *
 * Created by patricktwohig on 6/15/17.
 */
public class FacebookBuiltinPermissionsSupplier implements Supplier<List<FacebookPermission>> {

    private final List<FacebookPermission> facebookPermissionList;

    public FacebookBuiltinPermissionsSupplier() {

        final var classLoader = getClass().getClassLoader();

        final var result = new ClassGraph()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader)
                .enableClassInfo()
                .acceptPackages("dev.getelements")
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan();

        try (result) {

            final var classList = result.getClassesWithAnnotation(FacebookPermissions.class);

            facebookPermissionList = classList
                    .stream()
                    .map(ClassInfo::loadClass)
                    .map(c -> c.getAnnotation(FacebookPermissions.class))
                    .filter(Objects::nonNull)
                    .flatMap(a -> stream(a.value()))
                    .collect(Collectors.toList());

        }

    }

    @Override
    public List<FacebookPermission> get() {
        return new ArrayList<>(facebookPermissionList);
    }

}

