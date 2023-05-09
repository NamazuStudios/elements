package dev.getelements.elements.rt;

/**
 * Provides a reason as to why a task was resumed, typically by the {@link SchedulerContext}, but
 * not always.
 */
public enum ResumeReason {

    /**
     * The coroutine was resumed by the {@link SchedulerContext}
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
