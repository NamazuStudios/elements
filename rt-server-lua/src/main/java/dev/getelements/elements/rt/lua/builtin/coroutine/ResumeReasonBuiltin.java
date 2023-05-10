package dev.getelements.elements.rt.lua.builtin.coroutine;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import dev.getelements.elements.rt.ResumeReason;
import dev.getelements.elements.rt.lua.builtin.Builtin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResumeReasonBuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(ResumeReasonBuiltin.class);

    public static final String MODULE_NAME = CoroutineBuiltin.MODULE_NAME + ".resumereason";

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {
            @Override
            public String getChunkName() {
                return MODULE_NAME;
            }

            @Override
            public boolean exists() {
                return MODULE_NAME.equals(moduleName);
            }
        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {

            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);
            logger.debug("Loading module {} - {}", name, module.getChunkName());

            luaState.setTop(0);
            luaState.newTable();

            for (final ResumeReason resumeReason : ResumeReason.values()) {
                luaState.pushJavaObject(resumeReason.toString());
                luaState.setField(-2, resumeReason.toString());
            }

            return 1;

        };
    }

}
