package com.namazustudios.socialengine.rt.lua.builtin;

import com.google.common.base.Function;
import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.JavaReflector;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.exception.InternalException;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

/**
 * Allows a Java object to behave as a module.
 */
public class JavaObjectModuleBuiltin implements Builtin {



    private final String moduleName;

    private final Provider<?> provider;

    private final Function<String, String> caseConverter;

    private Map<String, List<Method>> methodCache = new HashMap<>();

    public <T> JavaObjectModuleBuiltin(final String moduleName,
                                       final Provider<T> tProvider) {
        this(moduleName, tProvider, in -> LOWER_UNDERSCORE.to(LOWER_UNDERSCORE, in));
    }

    public <T> JavaObjectModuleBuiltin(final String moduleName,
                                       final Provider<T> tProvider,
                                       final Function<String, String> caseConverter) {
        this.moduleName = moduleName;
        this.provider = tProvider;
        this.caseConverter = caseConverter;
    }

    @Override
    public Module getModuleNamed(final String name) {
        return new Module() {

            @Override
            public String getChunkName() {
                return moduleName;
            }

            @Override
            public boolean exists() {
                return moduleName.equals(name);
            }

        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {

            final Object object = provider.get();

            final JavaReflector javaReflector = metamethod -> {
                switch (metamethod) {
                    case INDEX: return index(object);
                    default:    return null;
                }
            };

            luaState.pushJavaObject(javaReflector);
            return 1;

        };
    }

    private JavaFunction index(final Object object) {
        return luaState -> {
            final String methodName = luaState.toString(2);
            final List<Method> methodList = getMethodsNamed(object, methodName);
            final JavaFunction dispatcherForMethod = getDispatcherForMethods(luaState, methodName, methodList);
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
                .orElseThrow(() -> new InternalException(target + " does not match method " + methodName));

            final Object[] args = new Object[nargs];
            final Class<?>[] parameterTypes = toInvoke.getParameterTypes();

            for (int i = 1; i < nargs; ++i) {
                args[i] = luaState.toJavaObject(i, parameterTypes[i]);
            }

            try {
                luaState.pushJavaObject(toInvoke.invoke(target, args));
                return 1;
            } catch (InvocationTargetException | IllegalAccessException ex) {
                throw new InternalException("Could not invoke Java method.", ex);
            }

        };

    }

    private boolean parametersMatch(final LuaState luaState, final Method method) {

        final int nargs = luaState.getTop();
        final Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 1; i < nargs; ++i) {
            if (!luaState.isJavaObject(i, parameterTypes[i])) {
                return false;
            }
        }

        return true;

    }

}
