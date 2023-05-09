package dev.getelements.elements.rt.lua;

import dev.getelements.elements.jnlua.JavaFunction;
import dev.getelements.elements.jnlua.LuaState;
import dev.getelements.elements.rt.exception.InternalException;

import java.util.function.Consumer;

public class ScriptAssert implements JavaFunction {

    private final Consumer<String> messageConsumer;

    public ScriptAssert() {
        this(s -> {});
    }

    public ScriptAssert(Consumer<String> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public int invoke(final LuaState luaState) {

        if (luaState.toBoolean(1)) {
            return 0;
        }

        final String msg = luaState.getTop() < 2 ? "assertion failure" : luaState.toString(2);
        messageConsumer.accept(msg);
        throw new InternalException(msg);

    }

}
