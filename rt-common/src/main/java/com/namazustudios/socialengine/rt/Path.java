package com.namazustudios.socialengine.rt;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents the path scheme for use in the server.  NOte that this class is not intended to be serialized
 * over the network, but is rather intended to be used on the client and the server to assist with matching
 * logic etc.
 *
 * Created by patricktwohig on 9/4/15.
 */
public final class Path implements Comparable<Path> {

    /**
     * The path separator.  Literal value "/"
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * The wildcard character.  Literal value "*"
     */
    public static final String WILDCARD = "*";

    private final List<String> components;

    // Kind of confusing, but this indicates the maximum index at which the
    // compareTo method should compare.
    private final int maxCompareIndex;

    private final boolean wildcard;

    /**
     * Parses the path into components and checks for hte wildcard character.
     *
     * @param path
     */
    public Path(final String path) {
        this(Util.componentsFromPath(path));
    }

    /**
     * Creates a {@link Path} with the path relative to the given path.
     *
     * @param parent the parent path
     * @param path the path
     *
     */
    public Path(final Path parent, final Path path) {
        this(Lists.newArrayList(Iterables.concat(parent.getComponents(), path.getComponents())));
    }

    /**
     * Creates a path with components and the wildcard flag.
     *
     * @param components the path components
     */
    public Path(final List<String> components) {
        final int idx = components.indexOf(WILDCARD);
        wildcard = idx >= 0;
        maxCompareIndex = wildcard ? idx : components.size();
        this.components = new ImmutableList.Builder<String>().addAll(components).build();
    }

    /**
     * Gets the components of this path.
     *
     * @return the components of this path
     */
    public List<String> getComponents() {
        return components;
    }

    /**
     * True if the path is a wildcard.
     *
     * @return true
     */
    public boolean isWildcard() {
        return wildcard;
    }

    /**
     * Checks if this path matches the other path.  Note taht this considers wildcards
     * whereas the {@link #hashCode()} and {@link #equals(Object)} methods determine
     * absolute equality.
     *
     * @param other the other path
     *
     * @return true if this path matches the other
     */
    public boolean matches(final Path other) {
        return compareTo(other) == 0;
    }

    /**
     * Returns the normalized path string.  NOte that {@link #toString()} does not return
     * a properly formatted path.  But rather a path useful for debugging and logging information.
     * To get the normalzied path, this method must be used.
     *
     * @return the normalized path as a string
     */
    public String toNormalizedPathString() {
        return Util.pathFromComponents(components);
    }

    @Override
    public String toString() {
        return "Path{" +
                "components=" + components +
                ", maxCompareIndex=" + maxCompareIndex +
                ", wildcard=" + wildcard +
                ", normalizedPath=" + toNormalizedPathString() +
                '}';
    }

    /**
     * This implementation of {@link #compareTo(Path)} compares the path to the other path
     * considering wild card matching and can beused to find paths in a sorted collection.
     *
     * @param other the other path
     * @return @see {@link Comparable#compareTo(Object)}
     */
    @Override
    public int compareTo(final Path other) {

        if (getComponents().size() != other.getComponents().size()) {
            return getComponents().size() - other.getComponents().size();
        }

        final Iterator<String> o1StringIterator;
        final Iterator<String> o2StringIterator;

        if (isWildcard() || other.isWildcard()) {
            final int min = Math.min(maxCompareIndex, other.maxCompareIndex);
            o1StringIterator = Iterators.limit(getComponents().iterator(), min);
            o2StringIterator = Iterators.limit(other.getComponents().iterator(), min);
        } else {
            o1StringIterator = getComponents().iterator();
            o2StringIterator = other.getComponents().iterator();
        }

        int value = 0;

        while (o1StringIterator.hasNext() && o2StringIterator.hasNext() && value == 0) {
            final String s1 = Strings.nullToEmpty(o1StringIterator.next());
            final String s2 = Strings.nullToEmpty(o2StringIterator.next());
            value = s1.compareTo(s2);
        }

        return value;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;

        Path path = (Path) o;

        if (isWildcard() != path.isWildcard()) return false;
        return components.equals(path.components);

    }

    @Override
    public int hashCode() {
        int result = components.hashCode();
        result = 31 * result + (isWildcard() ? 1 : 0);
        return result;
    }

    /**
     * Some utility methods used by all Resource and related instances.
     */
    public static final class Util {

        private Util() {}

        private static Pattern SPLIT_PATTERN = Pattern.compile("/+");

        /**
         * Gets the path components from the given path.
         *
         * @param path the path
         * @return the components
         */
        public static List<String> componentsFromPath(final String path) {
            return Splitter.on(SPLIT_PATTERN)
                .omitEmptyStrings()
                .trimResults()
                .splitToList(path);
        }


        /**
         * Joins the given string components together to build a path string from
         * the given componenets.
         *
         * @param pathComponents
         * @return the string
         */
        public static String pathFromComponents(final List<String> pathComponents) {

            final StringBuilder stringBuilder = Joiner.on(PATH_SEPARATOR)
                    .skipNulls()
                    .appendTo(new StringBuilder("/"), pathComponents);

            return stringBuilder.toString();

        }

        /**
         * Normalizes the path by removing duplicate separators, trimming whitespace, and then
         * rejoining into a single path wiht a leading separator.
         *
         * @param path the path to normailze
         * @return the normalized path
         */
        public String normalize(final String path) {
            final List<String> pathComponents = componentsFromPath(path);
            return pathFromComponents(pathComponents);
        }

    }

}
