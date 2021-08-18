package com.namazustudios.socialengine.doclet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A type which represents a compound description. Parts of the description can be
 */
public class CompoundDescription implements Iterable<String> {

    private final String separator;

    private final Deque<String> components = new ArrayDeque<>();

    /**
     * Constructs the {@link CompoundDescription} with a default separator.
     */
    public CompoundDescription() {
        this("");
    }

    /**
     * Constructs the {@link CompoundDescription} with a specific separator.
     */
    public CompoundDescription(final String separator) {
        this.separator = separator;
    }

    /**
     * Forms a {@link String} from the components of this {@link CompoundDescription}
     *
     * @return the description
     */
    public String getDescription() {
        return getDescription(separator);
    }

    /**
     * Gets a {@link String} from the components of this {@link CompoundDescription}, specifying a separator when
     * concatenating the strings.
     *
     * @param separator the separator
     * @return the description
     */
    public String getDescription(final String separator) {
        return components.stream().collect(Collectors.joining(separator));
    }

    /**
     * Sets the {@link CompoundDescription} to the supplied {@link String}. This will replace all components with the
     * supplied {@link String}
     *
     * @param description the description
     */
    public void setDescription(final String description) {
        this.components.clear();
        this.components.add(description);
    }

    /**
     * Appends a description to the end.
     *
     * @param description the description to append
     */
    public void appendDescription(final String description) {
        components.addLast(description);
    }

    /**
     * Prepends a description to the beginning.
     *
     * @param description the description to prepend
     */
    public void prependDescription(final String description) {
        components.addFirst(description);
    }

    /**
     * Streams all components.
     *
     * @return the {@link String<String>}
     */
    public Stream<String> stream() {
        return components.stream();
    }

    /**
     * Iterates over all components.
     *
     * @return an {@link Iterator<String>}
     */
    @Override
    public Iterator<String> iterator() {
        return components.iterator();
    }

    /**
     * Equivalent to calling {@link #getDescription()}.
     *
     * @return the description
     */
    @Override
    public String toString() {
        return getDescription();
    }

}
