package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.namazustudios.socialengine.rt.Resource;

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
    UNTIL_TIME,

    /**
     * Indicates that the should parse the second argument as a cron string.  The coroutine will resume when at the
     * next time the cron expression will hold true.
     */
    UNTIL_NEXT,

    /**
     * Indicates that the coroutine should yield for the specified amount of time.  The units may be specified but
     * the default is is in seconds.
     */
    FOR,

    /**
     * Indicates that the coroutine should yield indefinitely.  Typically this means the the calling code will manually
     * resume the coroutine at some point later.  Note that under some circumstances, the container may opt to forcibly
     * kill the running {@link Resource}
     * }
     */
    INDEFINITELY

}
