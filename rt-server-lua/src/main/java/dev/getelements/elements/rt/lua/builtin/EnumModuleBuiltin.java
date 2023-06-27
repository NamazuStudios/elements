package dev.getelements.elements.rt.lua.builtin;

import dev.getelements.elements.rt.annotation.CaseFormat;
import com.namazustudios.socialengine.jnlua.JavaFunction;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.rt.annotation.CaseFormat.UPPER_UNDERSCORE;
import static dev.getelements.elements.rt.lua.builtin.BuiltinDefinition.fromDefinition;
import static dev.getelements.elements.rt.lua.builtin.BuiltinDefinition.fromModuleName;

public class EnumModuleBuiltin<T extends Enum<T>> implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(EnumModuleBuiltin.class);

    private final Class<T> enumClass;

    private final BuiltinDefinition builtinDefinition;

    public EnumModuleBuiltin(Class<T> enumClass, final String moduleName) {
        this.enumClass = enumClass;
        this.builtinDefinition = fromModuleName(moduleName);
    }

    public EnumModuleBuiltin(Class<T> enumClass, final ModuleDefinition moduleDefinition) {
        this.enumClass = enumClass;
        this.builtinDefinition = fromDefinition(moduleDefinition);
    }

    @Override
    public Module getModuleNamed(final String moduleName) {

        final var enumModuleName = this.builtinDefinition.getModuleName();

        return new Module() {

            @Override
            public String getChunkName() {
                return enumModuleName;
            }

            @Override
            public boolean exists() {
                return enumModuleName.equals(moduleName);
            }

        };

    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {

            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);
            logger.debug("Loading module {} - {}", name, module.getChunkName());

            // build a lua table of the form {enumElement.toString(): enumElement}
            luaState.setTop(0);
            luaState.newTable();

            for (final Enum<T> enumValue : enumClass.getEnumConstants()) {

                final var enumValueString = UPPER_UNDERSCORE.to(
                    builtinDefinition.getConstantCaseFormat(),
                    enumValue.toString()
                );

                luaState.pushJavaObject(enumValue);
                luaState.setField(-2, enumValueString);

            }

            return 1;

        };
    }

}
