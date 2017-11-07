package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.TaskId;
import com.namazustudios.socialengine.rt.lua.builtin.coroutine.CoroutineBuiltin;

import static com.namazustudios.socialengine.rt.lua.Constants.REQUIRE;

public interface BuiltinUtils {

    /**
     * Invokes namazu_coroutine.current_task in the supplied {@link LuaState}.
     *
     * @param luaState the {@link LuaState}
     * @return the {@link TaskId} of the current task
     */
    static TaskId currentTaskId(final LuaState luaState) {

        luaState.getGlobal(REQUIRE);
        luaState.pushString(CoroutineBuiltin.MODULE_NAME);
        luaState.call(1, 1);
        luaState.getField(-1, CoroutineBuiltin.CURRENT_TASK_ID);
        luaState.remove(-2);
        luaState.call(0, 1);

        final String taskId = luaState.toString(-1);
        luaState.pop(1);
        return new TaskId(taskId);

    }

}
