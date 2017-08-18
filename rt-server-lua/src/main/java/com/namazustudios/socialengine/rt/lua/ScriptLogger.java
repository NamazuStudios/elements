package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;

import java.util.function.Consumer;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class ScriptLogger implements JavaFunction {

    private final Consumer<String> messageConsumer;

    public ScriptLogger(Consumer<String> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public int invoke(final LuaState luaState) {
        try (final StackProtector stackProtector = new StackProtector(luaState)) {

            final StringBuffer stringBuffer = new StringBuffer();

            for (int i = 1; i <= luaState.getTop(); ++i) {
                stringBuffer.append(luaState.toJavaObject(i, String.class));
            }

            messageConsumer.accept(stringBuffer.toString());
            return stackProtector.setAbsoluteIndex(0);

        }
    }

}
