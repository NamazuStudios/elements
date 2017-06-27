package com.namazustudios.socialengine;

import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.annotation.FacebookPermissions;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        this(ClassLoader.getSystemClassLoader());
    }

    public FacebookBuiltinPermissionsSupplier(final ClassLoader classLoader) {

        final Reflections reflections = new Reflections("com.namazustudios", classLoader);
        final Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(FacebookPermissions.class);

        facebookPermissionList = classSet.stream()
            .map(c -> c.getAnnotation(FacebookPermissions.class))
            .filter(a -> a != null)
            .flatMap(a -> stream(a.value()))
            .collect(Collectors.toList());

    }

    @Override
    public List<FacebookPermission> get() {
        return new ArrayList<>(facebookPermissionList);
    }

}

