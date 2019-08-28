package com.namazustudios.socialengine.rt.jeromq;

import java.util.Objects;

class JeroMQAsyncConnectionHandle implements Comparable<JeroMQAsyncConnectionHandle> {

    public final int index;

    public final JeroMQAsyncThreadContext context;

    public JeroMQAsyncConnectionHandle(final int index, final JeroMQAsyncThreadContext context) {
        this.index = index;
        this.context = context;
    }

    public int getIndex() {
        return index;
    }

    public JeroMQAsyncThreadContext getContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JeroMQAsyncConnectionHandle)) return false;
        JeroMQAsyncConnectionHandle handle = (JeroMQAsyncConnectionHandle) o;
        return index == handle.index && context.equals(handle.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, context);
    }

    @Override
    public int compareTo(final JeroMQAsyncConnectionHandle other) {
        return Integer.compare(index, other.index);
    }

}
