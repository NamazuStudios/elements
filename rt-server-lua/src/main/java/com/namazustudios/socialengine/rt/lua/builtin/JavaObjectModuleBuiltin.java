package com.namazustudios.socialengine.rt.lua.builtin;

import com.google.common.base.Function;
import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.JavaReflector;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.CurrentResource;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.persist.ErisPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.annotation.CaseFormat.LOWER_CAMEL;
import static com.namazustudios.socialengine.rt.annotation.CaseFormat.LOWER_UNDERSCORE;
import static com.namazustudios.socialengine.rt.lua.builtin.BuiltinDefinition.fromDefinition;
import static com.namazustudios.socialengine.rt.lua.builtin.BuiltinDefinition.fromModuleName;
import static com.namazustudios.socialengine.rt.lua.persist.ErisPersistence.mangle;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

/**
 * Allows a Java object to behave as a module.
 */
public class JavaObjectModuleBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(JavaObjectModuleBuiltin.class);

    private final Provider<?> provider;

    private final BuiltinDefinition builtinDefinition;

    private final Function<String, String> caseConverter;

    private Map<String, List<Method>> methodCache = new HashMap<>();

    private Consumer<JavaReflector> persistenceJavaReflectorConsumer = r -> {};

    public <T> JavaObjectModuleBuiltin(final String moduleName,
                                       final Provider<T> tProvider) {
        this(fromModuleName(moduleName), tProvider, in -> LOWER_UNDERSCORE.to(LOWER_CAMEL, in));
    }

    public <T> JavaObjectModuleBuiltin(final ModuleDefinition definition,
                                       final Provider<T> tProvider) {
        this(fromDefinition(definition), tProvider, in -> LOWER_UNDERSCORE.to(LOWER_CAMEL, in));
    }

    public <T> JavaObjectModuleBuiltin(final BuiltinDefinition builtinDefinition,
                                       final Provider<T> tProvider,
                                       final Function<String, String> caseConverter) {
        this.provider = tProvider;
        this.caseConverter = caseConverter;
        this.builtinDefinition = builtinDefinition;
    }

    @Override
    public Module getModuleNamed(final String name) {
        return new Module() {

            @Override
            public String getChunkName() {
                return builtinDefinition.getModuleName();
            }

            @Override
            public boolean exists() {
                return builtinDefinition.getModuleName().equals(name);
            }

        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {
            final JavaReflector javaReflector = makeJavaReflector();

            if (builtinDefinition.isDeprecated()) {

                Logger logger;

                try {
                    final var current = CurrentResource.getInstance().getCurrent();
                    logger = current.getLogger();
                } catch (IllegalStateException ex) {
                    // This shouldn't happen but if it does happen, we can continue on but it's an indicator that
                    // something else is wrong in the code.
                    logger = JavaObjectModuleBuiltin.logger;
                    logger.warn("No current resource.", ex);
                }

                logger.warn("{} is deprecated: {}",
                    builtinDefinition.getModuleName(),
                    builtinDefinition.getDeprecationWarning());

            }

            luaState.pushJavaObjectRaw(javaReflector);
            return 1;
        };
    }

    @Override
    public void makePersistenceAware(final ErisPersistence erisPersistence) {

        final String type = mangle(JavaObjectModuleBuiltin.class, builtinDefinition.getModuleName());

        erisPersistence.addCustomUnpersistence(type, l -> {
            l.pushJavaObject(makeJavaReflector());
            return 1;
        });

        persistenceJavaReflectorConsumer = r -> {
            erisPersistence.addCustomPersistence(r, type, l -> {
                l.pushNil();
                return 1;
            });
        };

    }

    private JavaReflector makeJavaReflector() {

        final Object object = provider.get();

        final JavaReflector javaReflector = metamethod -> {
            switch (metamethod) {
                case INDEX: return index(object);
                default:    return null;
            }
        };

        persistenceJavaReflectorConsumer.accept(javaReflector);

        return javaReflector;

    }

    private JavaFunction index(final Object object) {
        return luaState -> {

            final String methodName = luaState.toString(2);
            final List<Method> methodList = getMethodsNamed(object, methodName);

            if (methodList.isEmpty()) {
                return 0;
            }

            final JavaFunction dispatcherForMethod = getDispatcherForMethods(object, methodName, methodList);
            luaState.pushJavaFunction(dispatcherForMethod);
            return 1;

        };
    }

    private List<Method> getMethodsNamed(final Object object, final String methodName) {
        return methodCache.computeIfAbsent(methodName, k -> {

            final String converted = caseConverter.apply(methodName);

            final List<Method> methodList = stream(object.getClass().getMethods())
                .filter(m -> converted.equals(m.getName()))
                .collect(Collectors.toList());

            return unmodifiableList(methodList);

        });
    }

    public JavaFunction getDispatcherForMethods(final Object target,
                                                final String methodName,
                                                final List<Method> methodList) {
        return luaState -> {

            final int nargs = luaState.getTop();

            final Method toInvoke = methodList
                .stream()
                .filter(m -> m.getParameterCount() == nargs)
                .filter(m -> parametersMatch(luaState, m))
                .findFirst()
                .orElseThrow(() -> new InternalException("parameters do not match" + target + "." + methodName));

            final Object[] args = new Object[nargs];
            final Class<?>[] parameterTypes = toInvoke.getParameterTypes();

            for (int i = 0; i < nargs; ++i) {
                args[i] = luaState.toJavaObject(i + 1, parameterTypes[i]);
            }

            try {
                if (void.class.equals(toInvoke.getReturnType()) || Void.class.equals(toInvoke.getReturnType())) {
                    toInvoke.invoke(target, args);
                    return 0;
                } else {
                    luaState.pushJavaObject(toInvoke.invoke(target, args));
                    return 1;
                }
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            } catch (IllegalAccessException ex) {
                throw new InternalException("Could not invoke Java method.", ex);
            }

        };

    }

    private boolean parametersMatch(final LuaState luaState, final Method method) {

        final var parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!luaState.isJavaObject(i + 1, parameterTypes[i])) {
                return false;
            }
        }

        return true;

    }

}
