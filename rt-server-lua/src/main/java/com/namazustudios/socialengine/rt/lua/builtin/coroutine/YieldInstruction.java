package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

public enum YieldInstruction {

    /**
     * Indicates the coroutine should yield and be rescheduled as soon as absolutely possible.
     */
    IMMEDIATE,

    /**
     * Indicates that the coroutine should yield and be rescheduled until the provided absolute time.  The time is
     * expressed in the time since the unix epoch: January 1, 1970.  The units may be specified, but the default
     * value is in seconds.
     */
    UNTIL,

    /**
     * Indicates that the coroutine should yield for the specified amount of time.  The units may be specified but
     * the default is is in seconds.
     */
    FOR

}
