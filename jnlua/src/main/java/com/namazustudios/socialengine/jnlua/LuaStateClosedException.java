package com.namazustudios.socialengine.jnlua;

public class LuaStateClosedException extends IllegalStateException {

    public LuaStateClosedException() {}

    public LuaStateClosedException(String s) {
        super(s);
    }

    public LuaStateClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LuaStateClosedException(Throwable cause) {
        super(cause);
    }

}
