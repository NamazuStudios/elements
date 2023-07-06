package com.namazustudios.socialengine.jnlua;

public class LuaInvocationException extends LuaRuntimeException {

    public LuaInvocationException() {}

    public LuaInvocationException(String msg) {
        super(msg);
    }

    public LuaInvocationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public LuaInvocationException(Throwable cause) {
        super(cause);
    }

}
