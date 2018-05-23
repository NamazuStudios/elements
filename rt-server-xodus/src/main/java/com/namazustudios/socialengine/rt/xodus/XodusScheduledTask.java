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

    public static final String SEPARATOR = "::";

    public static final Pattern SEPARATOR_PATTERH = Pattern.compile("::");

    private final ResourceId resourceId;

    private final TaskId taskId;

    private final long when;

    public XodusScheduledTask(final ByteIterable key, final ByteIterable value) {

        final String[] keyComponents = SEPARATOR_PATTERH.split(entryToString(key));

        if (keyComponents.length != 1) {
            throw new IllegalArgumentException("Expecting two components.");
        }

        resourceId = new ResourceId(keyComponents[0]);
        taskId = new TaskId(keyComponents[1]);
        when = Long.valueOf(entryToString(value));

    }

    public XodusScheduledTask(final ResourceId resourceId, final TaskId taskId,
                              final long duration, final TimeUnit timeUnit) {
        this(resourceId, taskId, currentTimeMillis() + MILLISECONDS.convert(duration, timeUnit));
    }

    public XodusScheduledTask(final ResourceId resourceId, final TaskId taskId, final long when) {
        this.when = when;
        this.taskId = taskId;
        this.resourceId = resourceId;
    }

    public ByteIterable getKey() {
        return stringToEntry(format("%s%s%s", getResourceId().asString(), SEPARATOR, getTaskId().asString()));
    }

    public ByteIterable getValue() {
        return stringToEntry(Long.toString(getWhen()));
    }

    public long getWhen() {
        return when;
    }

    public ResourceId getResourceId() {
        return resourceId;
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
