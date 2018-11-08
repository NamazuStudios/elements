package com.namazustudios.socialengine.rt;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterators.limit;
import static com.google.common.collect.Lists.newArrayList;
import static com.namazustudios.socialengine.rt.Path.Util.*;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * Represents the path scheme for use in the server.
 *
 * Created by patricktwohig on 9/4/15.
 */
public class Path implements Comparable<Path>, Serializable {

    /**
     * The path separator.  Literal value "/"
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * The extension separator.
     */
    public static final String EXTENSION_SEPARATOR = ".";

    /**
     * The wildcard character.  Literal value "*"
     */
    public static final String WILDCARD = "*";

    /**
     * A {@link Pattern} to match valid path components.
     */
    public static final Pattern VALID_PATH_COMPONENT = Pattern.compile("[\\p{Print}]+");

    /**
     * The default encoding for converting a {@link Path} into an array of bytes.
     */
    public static final Charset ENCODING = Charset.forName("UTF-8");

    private final List<String> components;

    // Kind of confusing, but this indicates the maximum index at which the
    // compareTo method should compare.
    private final int maxCompareIndex;

    // A boolean value to indicate if the path is a wildcard path.
    private final boolean wildcard;

    private Path() {
        // This constructor must exist to ensure that the object can be serialized.  Serialization is able to break
        // encapsulation, invoke it, and forcibly set the members of the class.
        wildcard = false;
        maxCompareIndex = 0;
        components = emptyList();
    }

    /**
     * Parses the path into components and checks for hte wildcard character.
     *
     * @param path
     */
    public Path(final String path) {
        this(componentsFromPath(path));
    }

    /**
     * Creates a {@link Path} with the path relative to the given path.
     *
     * @param parent the parent path
     * @param path the path
     *
     */
    public Path(final Path parent, final Path path) {
        this(newArrayList(Iterables.concat(parent.getComponents(), path.getComponents())));
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
        this.components = unmodifiableList(components);

        this.components.forEach(c -> {
            if (!VALID_PATH_COMPONENT.matcher(c).matches()) {
                throw new IllegalArgumentException(c + " has invalid characters");
            }
        });

    }

    /**
     * Appends the following path to this path such that the final path is
     * expressed as follows:
     *
     * newPath = this/otherPath
     *
     * @param otherPath
     * @return
     */
    public Path append(final Path otherPath) {
        return new Path(this, otherPath);
    }

    /**
     * Appends an extension using the {@link #EXTENSION_SEPARATOR}.
     *
     * {@see {@link #appendExtension(String, String)}}
     *
     * @param extension the extension
     *
     * @return a new {@link Path}, applying the supplied extension
     */
    public Path appendExtension(final String extension) {
        return appendExtension(extension, EXTENSION_SEPARATOR);
    }

    /**
     * Appends an extension to this {@link Path}, using the supplied separator.  The final resulting {@link Path} is
     * the result of appending the separator and the extension to the last component of the string.
     *
     * @param extension the extension
     * @param separator the separator
     *
     * @return a new {@link Path}, applying the supplied extension
     */
    public Path appendExtension(final String extension, final String separator) {

        final List<String> components = new ArrayList<>(this.components);

        if (!components.isEmpty()) {
            final String last = components.remove(components.size() - 1);
            components.add(last + separator + extension);
        }

        return new Path(components);

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
     * Returns this {@link Path} as a non-wildcard path.  If the path is not a wildcard, this will simply return this
     * object.
     *
     * @return this path, stripping the wildcard.
     */
    public Path stripWildcard() {
        return isWildcard() ? new Path(components.subList(0, maxCompareIndex)) : this;
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
     * Returns this {@link Path} as an absolute path string.  This is essentially prepending
     * a '/' character to the result of {@link #toNormalizedPathString()}.
     *
     * @return the {@link Path} as represented by an absolute path.
     */
    public String toAbsolutePathString() {
        return '/' + toNormalizedPathString();
    }

    /**
     * Returns this {@link Path} as an absolute path string.  This is essentially prepending
     * a {@link File#pathSeparatorChar} to the result of {@link #toFileSystemPathString()}.
     *
     * @return the {@link Path} as represented by an absolute path.
     */
    public String toAbsoluteFileString() {
        return File.pathSeparatorChar + toFileSystemPathString();
    }

    /**
     * Returns the normalized path string.  Note that {@link #toString()} does not return
     * a properly formatted path.  But rather a path useful for debugging and logging information.
     * To get the normalzied path, this method must be used.
     *
     * @return the normalized path as a string
     */
    public String toNormalizedPathString() {
        return toNormalizedPathString(PATH_SEPARATOR);
    }

    /**
     * Returns the String representation of this Path as a file system path.
     *
     * @return the string representation
     */
    public String toFileSystemPathString() {
        return toNormalizedPathString(File.separator);
    }

    /**
     * Returns the normalized path string.  Note that {@link #toString()} does not return a properly formatted path.
     * But rather a path useful for debugging and logging information.
     *
     * @return the normalized path as a string
     */
    public String toNormalizedPathString(final String separator) {
        return pathFromComponents(components, separator);
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
     * considering wild card matching and can be used to find paths in a sorted collection.
     *
     * @param other the other path
     * @return @see {@link Comparable#compareTo(Object)}
     */
    @Override
    public int compareTo(final Path other) {

        final Iterator<String> o1StringIterator;
        final Iterator<String> o2StringIterator;

        if (isWildcard() || other.isWildcard()) {
            final int min = min(maxCompareIndex, other.maxCompareIndex);
            o1StringIterator = limit(getComponents().iterator(), min);
            o2StringIterator = limit(other.getComponents().iterator(), min);
        } else if (getComponents().size() != other.getComponents().size()) {
            return getComponents().size() - other.getComponents().size();
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

    public byte[] toByteArray() {
        return toNormalizedPathString().getBytes(ENCODING);
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
         * Gets the path components from the given path.
         *
         * @param path the path
         * @return the components
         */
        public static List<String> componentsFromPath(final String path, final String separator) {
            return Splitter.on(separator)
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(path);
        }

        /**
         * Joins the given string components together to build a path string from
         * the given components.
         *
         * @param pathComponents
         * @return the string
         */
        public static String pathFromComponents(final List<String> pathComponents) {
            return pathFromComponents(pathComponents, PATH_SEPARATOR);
        }

        /**
         * Joins the given string components together to build a path string from
         * the given components.
         *
         * @param pathComponents
         * @return the string
         */
        public static String pathFromComponents(final List<String> pathComponents, final String separator) {

            for (final String pathComponent : pathComponents) {
                if (pathComponent.contains(separator)) {
                    throw new IllegalArgumentException("Path components must not contain " + separator);
                }
            }

            return Joiner.on(separator).skipNulls().join(pathComponents);

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

    /**
     * Converts the supplied components to a {@link Path}.
     *
     * @param components the components in the {@link Path}
     *
     * @return the {@link Path}
     */
    public static Path fromComponents(String ... components) {
        return new Path(asList(components));
    }

    /**
     * Converts the supplied string representation of he {@link Path} using {@link #PATH_SEPARATOR} as the
     * separator.
     *
     * @param pathString the components in the {@link Path}
     *
     * @return the fully formed {@link Path}
     */
    public static Path fromPathString(final String pathString) {
        return fromPathString(pathString, PATH_SEPARATOR);
    }

    /**
     * Converts the supplied string representation of he {@link Path} with the supplied separator string.
     *
     * @param pathString the components in the {@link Path}
     * @param pathString the separator string
     *
     * @return the fully formed {@link Path}
     */
    public static Path fromPathString(final String pathString, final String separator) {
        return new Path(componentsFromPath(pathString, separator));
    }

}
