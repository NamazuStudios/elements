package com.namazustudios.socialengine.rt;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;
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
     * The separator of the context from the path components. Literal value "://", e.g. "myContext://foo/bar".
     */
    public static final String CONTEXT_SEPARATOR = "://";

    /**
     * The representation for the wildcard context. Literal value "*", e.g. "*://foo/bar".
     */
    public static final String WILDCARD_CONTEXT_REPRESENTATION = "*";

    /**
     * The representation of a context indicating that no context should be inferred. Literal value "?", e.g.
     * "?://foo/bar". When a path string with the NULL_CONTEXT_REPRESENTATION is provided, the generated {@link Path}
     * will have {@link Path#context} set to `null`.
     */
    public static final String NULL_CONTEXT_REPRESENTATION = "?";

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

    /**
     * The context for the path, e.g. `{nodeUuid}://path/to/resource`.
     */
    private String context = null;

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

    public Path(final List<String> components) {
        this(null, components);
    }

    /**
     * Creates a path with components and the wildcard flag.
     *
     * @param components the path components
     */
    public Path(final String context, final List<String> components) {
        this.context = context;


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
     * @param pathSeparator the separator
     *
     * @return a new {@link Path}, applying the supplied extension
     */
    public Path appendExtension(final String extension, final String pathSeparator) {

        final List<String> components = new ArrayList<>(this.components);

        if (!components.isEmpty()) {
            final String last = components.remove(components.size() - 1);
            components.add(last + pathSeparator + extension);
        }

        return new Path(components);

    }

    public String getContext() {
        return context;
    }

    public boolean hasContext() {
        if (context != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean hasWildcardContext() {
        if (hasContext() && context.equals(WILDCARD_CONTEXT_REPRESENTATION)) {
            return true;
        }
        else {
            return false;
        }
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
        return toNormalizedPathString(PATH_SEPARATOR, true);
    }

    /**
     * Returns the String representation of this Path as a file system path.
     *
     * @return the string representation
     */
    public String toFileSystemPathString() {
        return toNormalizedPathString(File.separator, false);
    }

    /**
     * Returns the normalized path string.  Note that {@link #toString()} does not return a properly formatted path.
     * But rather a path useful for debugging and logging information.
     *
     * @return the normalized path as a string
     */
    public String toNormalizedPathString(final String pathSeparator) {
        return toNormalizedPathString(pathSeparator, false);
    }

    public String toNormalizedPathString(final String pathSeparator, final boolean shouldIncludeContext) {
        final String context;
        if (shouldIncludeContext) {
            context = this.context;
        }
        else {
            context = null;
        }

        return pathFromContextAndComponents(context, components, pathSeparator);
    }

    @Override
    public String toString() {
        return "Path{" +
                "context='" + context + '\'' +
                ", components=" + components +
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
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return maxCompareIndex == path.maxCompareIndex &&
                isWildcard() == path.isWildcard() &&
                Objects.equals(getContext(), path.getContext()) &&
                Objects.equals(getComponents(), path.getComponents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getComponents(), maxCompareIndex, isWildcard());
    }

    public static final class ContextAndComponents {
        private final String context;
        private final List<String> components;

        public ContextAndComponents(final String context, final List<String> components) {
            this.context = context;
            this.components = components;
        }

        public String getContext() {
            return context;
        }

        public List<String> getComponents() {
            return components;
        }
    }

    /**
     * Some utility methods used by all Resource and related instances.
     */
    public static final class Util {

        private Util() {}

        private static Pattern SPLIT_PATTERN = Pattern.compile(PATH_SEPARATOR + "+");

        private static Pattern CONTEXT_SPLIT_PATTERN = Pattern.compile(CONTEXT_SEPARATOR + "+");

        /**
         * Gets the context from a path string, if it exists. Namely, it will return the string segment from the
         * beginning of the input string up to the first found location of `://`. If no such
         *
         * @param path the path to parse
         * @return the context if found, null otherwise
         */
        public static String contextFromPath(final String path) {
            final List<String> components = Splitter.on(CONTEXT_SPLIT_PATTERN)
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(path);

            if (components.size() != 2) {
                return null;
            }

            final String context = components.get(0);

            if (context.equals(NULL_CONTEXT_REPRESENTATION)) {
                return null;
            }

            return context;
        }

        public static ContextAndComponents contextAndComponentsFromPath(final String path) {
            return contextAndComponentsFromPath(path, PATH_SEPARATOR);
        }


        public static ContextAndComponents contextAndComponentsFromPath(final String path, final String pathSeparator) {
            if (!path.contains(CONTEXT_SEPARATOR)) {
                final List<String> components = componentsFromPath(path);

                return new ContextAndComponents(null, components);
            }

            final List<String> contextAndPath = Splitter.on(CONTEXT_SPLIT_PATTERN)
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(path);

            if (contextAndPath.size() != 2) {
                throw new IllegalArgumentException("Expected two results when splitting path with '://': " + path);
            }

            String context = contextAndPath.get(0);

            if (context.equals(NULL_CONTEXT_REPRESENTATION)) {
                context = null;
            }

            final List<String> components = componentsFromPath(path, pathSeparator);

            return new ContextAndComponents(context, components);
        }

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
        public static List<String> componentsFromPath(final String path, final String pathSeparator) {
            return Splitter.on(pathSeparator)
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(path);
        }

        public static String pathFromContextAndComponents(final String context, final List<String> pathComponents) {
            return pathFromContextAndComponents(context, pathComponents, PATH_SEPARATOR);
        }

        public static String pathFromContextAndComponents(final String context, final List<String> pathComponents, final String pathSeparator) {
            String resultPath = "";

            if (context != null) {
                final String trimmedContext = context.trim();

                if (trimmedContext.length() > 0 && !trimmedContext.equals(NULL_CONTEXT_REPRESENTATION)) {
                    resultPath = resultPath + trimmedContext + CONTEXT_SEPARATOR;
                }
            }

            resultPath = resultPath + pathFromComponents(pathComponents, pathSeparator);

            return resultPath;
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
        public static String pathFromComponents(final List<String> pathComponents, final String pathSeparator) {

            for (final String pathComponent : pathComponents) {
                if (pathComponent.contains(pathSeparator)) {
                    throw new IllegalArgumentException("Path components must not contain " + pathSeparator);
                }
            }

            return Joiner.on(pathSeparator).skipNulls().join(pathComponents);

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

    public static Path fromContextAndComponents(final String context, String ... components) {
        return new Path(context, asList(components));
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
    public static Path fromPathString(final String pathString, final String pathSeparator) {
        final ContextAndComponents contextAndComponents = contextAndComponentsFromPath(pathString, pathSeparator);
        return new Path(contextAndComponents.getContext(), contextAndComponents.getComponents());
    }

}
