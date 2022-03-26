package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InvalidNodeIdException;
import com.namazustudios.socialengine.rt.id.HasNodeId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.Path.Util.*;
import static com.namazustudios.socialengine.rt.id.NodeId.nodeIdFromString;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Represents the path scheme for use in the server.
 *
 * This implements {@link HasNodeId} which uses the {@link #getContext()} to attempt to derive the {@link NodeId} or
 * throw an exception if the context does not produce a valid {@link NodeId}.
 *
 * If the path has a wildcard context, then it returns a null {@link NodeId}.
 *
 * Created by patricktwohig on 9/4/15.
 */
public class Path implements Comparable<Path>, Serializable, HasNodeId {

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
    public static final Charset ENCODING = StandardCharsets.UTF_8;

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

    private transient volatile NodeId nodeId = null;

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
        this(parent.context, components(parent, path));
    }

    private static List<String> components(final Path parent, final Path path) {

        if (parent.hasContext() && !Objects.equals(path.context, parent.context)) {
            throw new IllegalArgumentException("context mismatch " + parent.context + "!=" + path.context);
        } if (!parent.hasContext() && path.hasContext()) {
            throw new IllegalArgumentException("child path must have no context or same as parent");
        }

        return concat(parent.components.stream(), path.components.stream()).collect(toList());

    }

    /**
     * Constructs a {@link Path} from the supplied components.
     *
     * @param components
     */
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

            if (c.contains(PATH_SEPARATOR)) {
                throw new IllegalArgumentException(c + " cannot contain separator.");
            }

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

    /**
     * Returns {@link Path} that is the parent to this {@link Path}, preserving the context (if any). If this path is
     * the root path (ie having no components), then this will return this object.
     *
     * @return the parent {@link Path}, or this if this is a root path
     */
    public Path parent() {
        return components.isEmpty() ? this : new Path(context, components.subList(0, components.size() - 1));
    }

    /**
     * Returns true if this is a root path (ie having no components).
     *
     * @return true if this is a root path
     */
    public boolean isRoot() {
        return components.isEmpty();
    }

    /**
     * Gets the context of this {@link Path}, or null if no context exists.
     *
     * @return the context or null
     */
    public String getContext() {
        return context;
    }

    /**
     * Returns true if this {@link Path} has a context.
     *
     * @return true if this {@link Path} has a context
     */
    public boolean hasContext() {
        return context != null;
    }

    /**
     * Returns true if this {@link Path} both has a context and that context is a wildcard context.
     *
     * @return true if the context is a wildcard context
     */
    public boolean isWildcardContext() {
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
        return isWildcard() ? new Path(context, components.subList(0, maxCompareIndex)) : this;
    }

    /**
     * Checks if this path matches the other path.  Note that this considers wildcards
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
        final String normalized = toNormalizedPathString();
        return hasContext() || normalized.startsWith(PATH_SEPARATOR) ? normalized : PATH_SEPARATOR + normalized;
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

    /**
     * Converts this {@link Path} to a {@link String} representing the path. Optionally including the context.
     *
     * @param pathSeparator the path separator
     * @param shouldIncludeContext
     * @return
     */
    public String toNormalizedPathString(final String pathSeparator, final boolean shouldIncludeContext) {

        final String context;

        if (shouldIncludeContext) {
            context = this.context;
        }
        else {
            context = null;
        }

        return stringFromContextAndComponents(context, components, pathSeparator);

    }

    public Path toPathWithContext(final String context) {
        if (this.context == null || this.context.equals(context)) {
            return new Path(context, components);
        } else {
            throw new IllegalArgumentException("Context mismatch.");
        }
    }

    @Override
    public NodeId getNodeId() throws InvalidNodeIdException {
        return !hasContext() ? null : nodeId == null ? (nodeId = nodeIdFromString(getContext())) : nodeId;
    }

    @Override
    public String toString() {
        return toNormalizedPathString();
    }

    /**
     * This implementation of {@link #compareTo(Path)} compares the path to the other path considering wild card
     * matching and can be used to find paths in a sorted collection.
     *
     * @param other the other path
     * @return @see {@link Comparable#compareTo(Object)}
     */
    @Override
    public int compareTo(final Path other) {

        final Iterator<String> o1StringIterator;
        final Iterator<String> o2StringIterator;

        if (isWildcard() || other.isWildcard()) {
            final int limit = min(maxCompareIndex, other.maxCompareIndex);
            o1StringIterator = getComponents().stream().limit(limit).iterator();
            o2StringIterator = other.getComponents().stream().limit(limit).iterator();
        } else if (getComponents().size() != other.getComponents().size()) {
            return getComponents().size() - other.getComponents().size();
        } else {
            o1StringIterator = getComponents().iterator();
            o2StringIterator = other.getComponents().iterator();
        }

        int value = 0;

        while (o1StringIterator.hasNext() && o2StringIterator.hasNext() && value == 0) {
            final String s1 = o1StringIterator.next();
            final String s2 = o2StringIterator.next();
            value = (s1 == null ? "" : s1).compareTo(s2 == null ? "" : s2);
        }

        return value;

    }

    /**
     * Gets a byte[] representation of this {@link Path}.
     *
     * @return the byte array
     */
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
     * Converts the supplied components to a {@link Path}.
     *
     * @param components the components in the {@link Path}
     *
     * @return the {@link Path}
     */
    public static Path fromComponents(final String ... components) {
        return new Path(asList(components));
    }

    /**
     * Gets the a {@link Path} from the supplied context and components.
     *
     * @param context the context
     * @param components the components
     * @return the {@link Path} instance
     */
    public static Path fromContextAndComponents(final String context, final String ... components) {
        return new Path(context, asList(components));
    }

    /**
     * Gets the a {@link Path} from the supplied context and components.
     *
     * @param hasNodeId the {@link HasNodeId} from which to derive the context string
     * @param components the components
     * @return the {@link Path} instance
     */
    public static Path fromContextAndComponents(final HasNodeId hasNodeId, final String ... components) {

        final String context = hasNodeId
            .getOptionalNodeId()
            .map(NodeId::asString)
            .orElse(null);

        return fromContextAndComponents(context, components);

    }

    /**
     * Converts the supplied byte array representation to a {@link Path}
     * @param pathBytes the bytes of the path
     * @return the {@link Path}
     */
    public static Path fromBytes(final byte[] pathBytes) {
        final var wrapped = ByteBuffer.wrap(pathBytes);
        return fromByteBuffer(wrapped);
    }

    private static Path fromByteBuffer(final ByteBuffer byteBuffer) {
        final var pathString = ENCODING.decode(byteBuffer).toString();
        return fromPathString(pathString);
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

            final List<String> components = Stream.of(CONTEXT_SPLIT_PATTERN.split(path))
                  .filter(c -> c != null)
                  .map(c -> c.trim())
                  .filter(c -> !c.isEmpty())
                  .collect(toList());

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

                final List<String> components = Stream.of(path.split(Pattern.quote(pathSeparator)))
                    .map(c -> c.trim())
                    .filter(c -> !c.isEmpty())
                    .collect(toList());

                return new ContextAndComponents(null, components);

            }

            final List<String> contextAndPath = Stream.of(CONTEXT_SPLIT_PATTERN.split(path))
                .map(c -> c.trim())
                .filter(c -> !c.isEmpty())
                .collect(toList());

            if (contextAndPath.size() != 2) {
                throw new IllegalArgumentException("Expected two results when splitting path with '://': " + path);
            }

            String context = contextAndPath.get(0);

            if (context.equals(NULL_CONTEXT_REPRESENTATION)) {
                context = null;
            }

            final String quoted = Pattern.quote(pathSeparator);
            final String pathComponent = contextAndPath.get(1);

            final List<String> components = Stream.of(pathComponent.split(quoted))
                .map(c -> c.trim())
                .filter(c -> ! c.isEmpty())
                .collect(toList());

            return new ContextAndComponents(context, components);

        }

        /**
         * Gets the path components from the given path.
         *
         * @param path the path
         * @return the components
         */
        public static List<String> componentsFromPath(final String path) {
            return contextAndComponentsFromPath(path).components;
        }

        /**
         * Gets the path components from the given path.
         *
         * @param path the path
         * @return the components
         */
        public static List<String> componentsFromPath(final String path, final String pathSeparator) {
            return contextAndComponentsFromPath(path, pathSeparator).components;
        }

        /**
         * Gets a {@link String} representing the a path and path components.
         *
         * @param context the context
         * @param pathComponents the path components
         * @return the {@link String} representation of a {@link Path}
         */
        public static String stringFromContextAndComponents(final String context,
                                                            final List<String> pathComponents) {
            return stringFromContextAndComponents(context, pathComponents, PATH_SEPARATOR);
        }

        /**
         * Gets a {@link String} representing the a path and path components.
         *
         * @param context the context
         * @param pathComponents the path components
         * @return the {@link String} representation of a {@link Path}
         */
        public static String stringFromContextAndComponents(final String context,
                                                            final List<String> pathComponents,
                                                            final String pathSeparator) {
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

            return pathComponents
                .stream()
                .filter(c -> c != null)
                .map(c -> c.trim())
                .collect(Collectors.joining(pathSeparator));

        }

        /**
         * Normalizes the path by removing duplicate separators, trimming whitespace, and then
         * rejoining into a single path wiith a leading separator.
         *
         * @param path the path to normalize
         * @return the normalized path
         */
        public String normalize(final String path) {
            final List<String> pathComponents = componentsFromPath(path);
            return pathFromComponents(pathComponents);
        }

    }

}
