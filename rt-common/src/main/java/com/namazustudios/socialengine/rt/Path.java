package com.namazustudios.socialengine.rt;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents the path scheme for use in the server.  NOte that this class is not intended to be serialized
 * over the network, but is rather intended to be used on the client and the server to assist with matching
 * logic etc.
 *
 * Created by patricktwohig on 9/4/15.
 */
public class Path implements Comparable<Path> {

    /**
     * The path separator.  Literal value "/"
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * The wildcard character.  Literal value "*"
     */
    public static final String WILDCARD = "*";

    private final List<String> components;

    private final boolean wildcard;

    /**
     * Parses the path into components and checks for hte wildcard character.
     *
     * @param path
     */
    public Path(String path) {
        this(Util.componentsFromPath(path));
    }

    /**
     * Creates a path with components and the wildcard flag.
     *
     * @param components
     */
    public Path(List<String> components) {
        this.components = new ImmutableList.Builder<String>().addAll(components).build();
        wildcard = !components.isEmpty() && WILDCARD.equals(components.subList(0, components.size() - 1));
    }

    /**
     * Returns this path as a string.
     *
     * @return the path
     */
    public String toString() {
        return Util.pathFromComponents(components);
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
     * @return
     */
    public boolean isWildcard() {
        return wildcard;
    }

    @Override
    public int compareTo(final Path other) {

        final Iterator<String> o1StringIterator = getComponents().iterator();
        final Iterator<String> o2StringIterator = other.getComponents().iterator();

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

        if (wildcard != path.wildcard) return false;
        return components.equals(path.components);

    }

    @Override
    public int hashCode() {
        int result = components.hashCode();
        result = 31 * result + (wildcard ? 1 : 0);
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
            return Splitter.on(SPLIT_PATTERN).trimResults().splitToList(path);
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
         * Normalizes the path by removing duplicate seprators, trimming whitespace, and then
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
