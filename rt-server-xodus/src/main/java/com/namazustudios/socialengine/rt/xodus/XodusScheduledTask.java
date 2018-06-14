package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.ResourceId;
import com.namazustudios.socialengine.rt.TaskId;
import jetbrains.exodus.ByteIterable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.bindings.StringBinding.stringToEntry;

public class XodusScheduledTask {

    private final TaskId taskId;

    private final long when;

    public XodusScheduledTask(final ByteIterable key, final ByteIterable value) {
        taskId = new TaskId(entryToString(key));
        when = Long.valueOf(entryToString(value));
    }

    public XodusScheduledTask(final TaskId taskId, final long duration, final TimeUnit timeUnit) {
        this(taskId, currentTimeMillis() + MILLISECONDS.convert(duration, timeUnit));
    }

    public XodusScheduledTask(final TaskId taskId, final long when) {
        this.when = when;
        this.taskId = taskId;
    }

    public ByteIterable getKey() {
        return stringToEntry(taskId.asString());
    }

    public ByteIterable getValue() {
        final String when = Long.toString(getWhen());
        return stringToEntry(when);
    }

    public long getWhen() {
        return when;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    @Override
    public String toString() {
        return "XodusScheduledTask{" +
                "when=" + when +
                ", taskId=" + taskId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XodusScheduledTask)) return false;
        XodusScheduledTask that = (XodusScheduledTask) o;
        return when == that.when && Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(when, taskId);
    }

}
