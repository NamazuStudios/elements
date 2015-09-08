package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 9/7/15.
 */
public class EventSubscriptionTuple implements Comparable<EventSubscriptionTuple> {

    final String name;
    final Path path;

    public EventSubscriptionTuple(final String name, final Path path) {
        this.name = name;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventSubscriptionTuple)) return false;

        EventSubscriptionTuple that = (EventSubscriptionTuple) o;

        if (!name.equals(that.name)) return false;
        return path.equals(that.path);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public int compareTo(final EventSubscriptionTuple other) {
        final int nameComparison = name.compareTo(other.name);
        return (nameComparison == 0) ? path.compareTo(other.path) : nameComparison;
    }

}
