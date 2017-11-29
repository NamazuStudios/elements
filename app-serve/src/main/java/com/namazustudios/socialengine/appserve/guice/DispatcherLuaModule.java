package com.namazustudios.socialengine.appserve.guice;

import com.namazustudios.socialengine.annotation.Expose;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import org.reflections.Reflections;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class DispatcherLuaModule extends LuaModule {

    @Override
    protected void configureFeatures() {

        super.configureFeatures();

        final Reflections reflections = new Reflections("com.namazustudios", getClass().getClassLoader());
        final Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(Expose.class);

        classSet.stream()
                .filter(cls -> cls.getAnnotation(Expose.class) != null)
                .collect(Collectors.toMap(cls -> cls.getAnnotation(Expose.class), identity()))
                .forEach((expose, type) -> bindBuiltin(type).toModuleNamed(expose.luaModuleName()));

    }

}
