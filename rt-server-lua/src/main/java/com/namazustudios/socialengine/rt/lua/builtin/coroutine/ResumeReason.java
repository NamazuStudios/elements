package com.namazustudios.socialengine.rt.lua.builtin.coroutine;

import com.namazustudios.socialengine.rt.Scheduler;

/**
 * Handed to server-managed coroutines to indicate why the coroutine was resumed.
 */
public enum ResumeReason {

    /**
     * The coroutine was resumed by the {@link Scheduler}
     */
    SCHEDULER,

    /**
     * The coroutine was resumed as the part of a network or distributed callback.
     */
    NETWORK,

    /**
     * Indicates that an error condition caused the coroutine to resume.  The re
     */
    ERROR,

    /**
     * The coroutine was manually resumed.
     */
    MANUAL

}
