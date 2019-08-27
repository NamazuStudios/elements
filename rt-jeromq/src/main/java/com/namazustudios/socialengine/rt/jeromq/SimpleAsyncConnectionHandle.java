package com.namazustudios.socialengine.rt.jeromq;

import java.util.Objects;

class SimpleAsyncConnectionHandle implements Comparable<SimpleAsyncConnectionHandle> {

    public final int index;

    public final SimpleAsyncThreadContext context;

    public SimpleAsyncConnectionHandle(final int index, final SimpleAsyncThreadContext context) {
        this.index = index;
        this.context = context;
    }

    public int getIndex() {
        return index;
    }

    public SimpleAsyncThreadContext getContext() {
        return context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleAsyncConnectionHandle)) return false;
        SimpleAsyncConnectionHandle handle = (SimpleAsyncConnectionHandle) o;
        return index == handle.index && context.equals(handle.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, context);
    }

    @Override
    public int compareTo(final SimpleAsyncConnectionHandle other) {
        return Integer.compare(index, other.index);
    }

}
