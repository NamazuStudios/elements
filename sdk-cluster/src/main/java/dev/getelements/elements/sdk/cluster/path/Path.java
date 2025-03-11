package dev.getelements.elements.sdk.cluster.path;

import dev.getelements.elements.sdk.cluster.id.exception.InvalidNodeIdException;
import dev.getelements.elements.sdk.cluster.id.HasNodeId;
import dev.getelements.elements.sdk.cluster.id.NodeId;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.cluster.id.NodeId.nodeIdFromString;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;
import static java.util.List.copyOf;
import static java.util.UUID.randomUUID;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
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
public final class Path implements Serializable, HasNodeId {

    /**
     * The separator of the context from the path components. Literal value "://", e.g. "myContext://foo/bar".
     */
    public static final String CONTEXT_SEPARATOR = "://";

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
     * The recursive-wildcard character.  Literal value "**"
     */
    public static final String WILDCARD_RECURSIVE = "**";

    /**
     * The representation for the wildcard context. Literal value "*", e.g. "*://foo/bar".
     *
     * @deprecated use {@link Path#WILDCARD}
     */
    public static final String WILDCARD_CONTEXT_REPRESENTATION = WILDCARD;

    /**
     * A {@link Pattern} to match valid path components.
     */
    public static final Pattern VALID_PATH_COMPONENT = Pattern.compile("\\p{Print}+");

    /**
     * The context split pattern.
     */
    private static final Pattern CONTEXT_SPLIT_PATTERN = Pattern.compile(quote(CONTEXT_SEPARATOR));

    /**
     * The default encoding for converting a {@link Path} into an array of bytes.
     */
    public static final Charset ENCODING = StandardCharsets.UTF_8;

    private final ContextAndComponents contextAndComponents;

    // A listing of all indices
    private final int[] wildcardIndices;

    private final boolean wildcardRecursive;

    private transient volatile NodeId nodeId = null;

    /**
     * Implements the conventional valueOf method by invoking {@link Path(String)}.
     *
     * @param string the path to parse
     * @return the {@link Path} valueOf
     */
    public static Path valueOf(final String string) {
        return new Path(string);
    }

    /**
     * Formats a {@link Path}.
     * @param fmt
     * @param fmtArgs
     * @return
     */
    public static Path formatPath(final String fmt, final Object ... fmtArgs) {
        final var pathString = format(fmt, fmtArgs);
        return new Path(pathString);
    }

    public Path() {
        this(new ContextAndComponents(null, emptyList()));
    }

    /**
     * Parses the path into components and checks for hte wildcard character.
     *
     * @param path the path as represented by a {@link String}
     */
    public Path(final String path) {
        this(contextAndComponentsFromPath(path, PATH_SEPARATOR));
    }

    /**
     * Creates a {@link Path} with the path relative to the given path.
     *
     * @param parent the parent path
     * @param path the path
     *
     */
    public Path(final Path parent, final Path path) {
        this(parent.getContext(), components(parent, path));
    }

    private static List<String> components(final Path parent, final Path path) {

        if (parent.hasContext() && path.hasContext() && !Objects.equals(path.getContext(), parent.getContext())) {
            throw new IllegalArgumentException("Parent path must match " + parent.getContext() + "!=" + path.getComponents());
        } if (!parent.hasContext() && path.hasContext()) {
            throw new IllegalArgumentException("Parent path must have context if child has context.");
        }

        return concat(parent.getComponents().stream(), path.getComponents().stream()).collect(toList());

    }

    /**
     * Constructs a {@link Path} from the supplied components.
     *
     * @param components the list of components
     */
    public Path(final List<String> components) {
        this(null, components);
    }

    /**
     * Creates a path with components and the wildcard flag.
     *
     * @param context the context
     * @param components the path components
     */
    public Path(final String context, final List<String> components) {
        this (new ContextAndComponents(context, components));
    }

    /**
     * Creates a path with the supplied {@link ContextAndComponents}
     *
     * @param contextAndComponents the {@link ContextAndComponents}
     */
    public Path(final ContextAndComponents contextAndComponents) {

        this.contextAndComponents = contextAndComponents;

        final var context = contextAndComponents.getContext();
        final var components = contextAndComponents.getComponents();

        final var wildcardRecursiveIndex = components.indexOf(WILDCARD_RECURSIVE);
        this.wildcardRecursive = wildcardRecursiveIndex >= 0;

        if (this.wildcardRecursive && wildcardRecursiveIndex != components.size() - 1) {
            throw new IllegalArgumentException("Wildcard recursive paths must end with " + WILDCARD_RECURSIVE);
        }

        this.wildcardIndices = IntStream.range(0,  components.size())
                .filter(i -> WILDCARD.equals(components.get(i)))
                .toArray();

        if (WILDCARD_RECURSIVE.equals(context)) {
            throw new IllegalArgumentException("Context cannot be: " + WILDCARD_RECURSIVE);
        }

        for (final var component : components) {

            if (component.contains(PATH_SEPARATOR)) {
                throw new IllegalArgumentException(component + " cannot contain separator.");
            }

            if (!VALID_PATH_COMPONENT.matcher(component).matches()) {
                throw new IllegalArgumentException(component + " has invalid characters");
            }

        }

    }

    /**
     * Appends the following path to this path such that the final path is
     * expressed as follows:
     * newPath = this/otherPath
     *
     * @param otherPath the other path to append
     * @return a new {@link Path}, appending the components of this path
     */
    public Path append(final Path otherPath) {
        return new Path(this, otherPath);
    }

    /**
     * Appends components to the path and returns a new {@link Path}.
     *
     * @param first the first component to add
     * @return the newly created path.
     */
    public Path appendComponents(final String first) {
        final var components = new ArrayList<>(getComponents());
        components.add(first);
        return new Path(getContext(), components);
    }

    /**
     * Appends components to the path and returns a new {@link Path}.
     *
     * @param first the first component to add
     * @param subsequent the subsequent components
     * @return the newly created path.
     */
    public Path appendComponents(final String first, final String ... subsequent) {
        final var components = new ArrayList<>(getComponents());
        components.add(first);
        addAll(components, subsequent);
        return new Path(getContext(), components);
    }

    /**
     * Appends a single component if this path ends in a wildcard or recursive wildcard, then this method will return
     * a new Path with the result of the {@link Supplier}. Otherwise, this method will return this instance as-is.
     *
     * This will avoid invoking the supplier in case the supplier exhausts a system resource, sou
     *
     * @param stringSupplier a supplier for the component to add.
     * @return the newly created path.
     */
    public Path appendIfWildcard(final Supplier<String> stringSupplier) {
        return isWildcardTerminated() || isWildcardRecursive()
                ? stripWildcardRecursive().stripWildcard(-1).appendComponents(stringSupplier.get())
                : this;
    }

    /**
     * Appends a UUID component if this path is a wildcard path.
     *
     *
     * @return the newly created path.
     */
    public Path appendUUIDIfWildcard() {
        return appendIfWildcard(() -> randomUUID().toString());
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

        final List<String> components = new ArrayList<>(this.getComponents());

        if (!components.isEmpty()) {
            final String last = components.remove(components.size() - 1);
            components.add(last + pathSeparator + extension);
        }

        return new Path(components);

    }

    /**
     * Returns a {@link Path} that is the parent to this {@link Path}, preserving the context (if any). If this path is
     * the root path (ie having no components), then this will return this object.
     *
     * @return the parent {@link Path}, or this if this is a root path
     */
    public Path parent() {
        final var context = getContext();
        final var components = getComponents();
        return components.isEmpty() ? this : new Path(context, components.subList(0, components.size() - 1));
    }

    /**
     * Returns a {@link Path} which is a recursive wildcard if this instance is not a recursive wildcard. If the path
     * is already a wildcard recursive, then this path will
     *
     * @return this {@link Path}
     */
    public Path toWildcardRecursive() {
        return !isWildcardRecursive()
                ? stripWildcard(-1).appendComponents(WILDCARD_RECURSIVE)
                : this;
    }

    /**
     * Returns true if this is a root path (ie having no components).
     *
     * @return true if this is a root path
     */
    public boolean isRoot() {
        return getComponents().isEmpty();
    }

    /**
     * Returns the root path of this one, preserving context.
     *
     * @return the root path
     */
    public Path contextRootPath() {
        return new Path(new ContextAndComponents(getContext(), emptyList()));
    }

    /**
     * Gets the context of this {@link Path}, or null if no context exists.
     *
     * @return the context or null
     */
    public String getContext() {
        return getContextAndComponents().getContext();
    }

    /**
     * Returns true if this {@link Path} has a context.
     *
     * @return true if this {@link Path} has a context
     */
    public boolean hasContext() {
        return getContextAndComponents().getContext() != null;
    }

    /**
     * Returns true if this {@link Path} both has a context and that context is a wildcard context.
     *
     * @return true if the context is a wildcard context
     */
    public boolean isWildcardContext() {
        return WILDCARD.equals(getContext());
    }

    /**
     * Gets the component of a Path at the supplied index. Additionally, this allows for negative numbers indicating
     * the reverse-order index of the list.
     *
     * @param index the index or reverse-index specified by a netagive integer
     */
    public String getComponent(int index) {
        return getContextAndComponents().getComponent(index);
    }

    /**
     * Gets the components of this path.
     *
     * @return the components of this path
     */
    public List<String> getComponents() {
        return getContextAndComponents().getComponents();
    }

    /**
     * Gets the {@link ContextAndComponents} instance for this path.
     *
     * @return the {@link ContextAndComponents} instance
     */
    public ContextAndComponents getContextAndComponents() {
        return contextAndComponents;
    }

    /**
     * Gets an {@link IntStream} of all wild card indices.
     *
     * @return the wildcard indices
     */
    public IntStream streamWildcardIndices() {
        return IntStream.of(wildcardIndices);
    }

    public List<Integer> getWildcardIndices() {
        return streamWildcardIndices().boxed().collect(toUnmodifiableList());
    }

    /**
     * True if the path is a wildcard.
     *
     * @return true if wildcard, false otherwise
     */
    public boolean isWildcard() {
        return wildcardIndices.length > 0;
    }

    /**
     * True if the path is a wildcard.
     *
     * @return true if wildcard, false otherwise
     */
    public boolean isWildcardTerminated() {
        return  wildcardRecursive ||
                wildcardIndices.length > 0 && wildcardIndices[wildcardIndices.length - 1] == getComponents().size() - 1;
    }

    /**
     * True if the path is a recursive wildcard.
     *
     * @return true if wildcard, false otherwise
     */
    public boolean isWildcardRecursive() {
        return wildcardRecursive;
    }

    /**
     * Returns a new {@link Path} which strips all components up to the wildcard index. The wildcard index is the zero
     * indexed n'th position of the {@link Path}. The index may be negative, indicating the wildcard index will be
     * stripped from the end of the indices. If the path has no wildcard components, then this will simply return
     * this object.
     *
     * @param wildcardIndex &gt;= 0 for the n'th from beginning, &lt;0 for the n'th index from the end
     * @return the {@link Path}
     */
    public Path stripWildcard(final int wildcardIndex) {

        if (wildcardIndices.length == 0) {
            return this;
        }

        final var componentIndex = wildcardIndices[
                wildcardIndex < 0
                        ? (wildcardIndices.length + wildcardIndex)
                        : wildcardIndex
                ];

        final var components = getComponents().subList(0, componentIndex);
        return new Path(getContext(), components);

    }

    /**
     * Returns this {@link Path} as a non-wildcard path.  If the path is not a wildcard, this will simply return this
     * object.
     *
     * @return this path, stripping the wildcard.
     */
    public Path stripWildcardRecursive() {
        final var context = getContext();
        final var components = getComponents();
        return wildcardRecursive
                ? new Path(context, components.subList(0, components.size() - 1))
                : this;
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
        return matches(this, other);
    }

    /**
     * Returns the normalized path string with context and default path separator using {@link Path#PATH_SEPARATOR} and
     * including context.
     *
     * @return the normalized path as a string
     */
    public String toNormalizedPathString() {
        return toNormalizedPathString(PATH_SEPARATOR, true);
    }

    /**
     * Returns the String representation of this Path as a file system path using {@link File#separator} and not
     * including context.
     *
     * @return the string representation
     * @deprecated use {@link #toRelativeFilesystemPath()} ()}
     */
    @Deprecated
    public String toFileSystemPathString() {
        return toNormalizedPathString(File.separator, false);
    }

    /**
     * Converts this path to a relative FS path.
     *
     * @return the {@link java.nio.file.Path}
     */
    public java.nio.file.Path toRelativeFilesystemPath() {

        final var components = getComponents();

        if (components.isEmpty()) {
            return java.nio.file.Path.of("");
        } else if (components.size() == 1) {
            return java.nio.file.Path.of(components.get(0));
        } else {
            return java.nio.file.Path.of(
                    components.get(0),
                    components.subList(1, components.size()).toArray(String[]::new)
            );
        }

    }

    /**
     * If this {@link Path} has no context, then this will return the path string w/ a path separator.
     *
     * @param pathSeparator the path separator
     * @return a {@link String} representing the relative portion of this path.
     */
    public String toRelativePathString(final String pathSeparator) {

        if (hasContext()) {
            throw new IllegalStateException("Must use on relative paths.");
        }

        return join(pathSeparator, getComponents());
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
     * @param shouldIncludeContext true if the context should be included
     * @return the String representing the path
     */
    public String toNormalizedPathString(final String pathSeparator, final boolean shouldIncludeContext) {
        final var context = getContext();
        final var components = getComponents();
        return context != null && shouldIncludeContext
                ? format("%s://%s", context, join(pathSeparator, components))
                : format("/%s", join(pathSeparator, components));
    }

    /**
     * Returns a Path which will have the context specified.
     *
     * @param newContext the context
     * @return the Path, or this if the context matches
     * @throws IllegalArgumentException if the context mismatches
     */
    public Path toPathWithContext(final String newContext) {
        return Objects.equals(getContext(), newContext)
                ? this
                : new Path(newContext, getComponents());
    }

    /**
     * Returns a Path which will have the context specified.
     *
     * @param hasNodeId the {@link HasNodeId} instance
     * @return the Path, or this if the context matches
     * @throws IllegalArgumentException if the context mismatches
     */
    public Path toPathWithContextIfAbsent(final HasNodeId hasNodeId) {
        return hasContext()
                ? this
                : new Path(hasNodeId.getNodeId().toString(), getComponents());
    }

    /**
     * Returns a Path which will have the context specified.
     *
     * @param newContext the context
     * @return the Path, or this if the context matches
     * @throws IllegalArgumentException if the context mismatches
     */
    public Path toPathWithContextIfAbsent(final String newContext) {
        return hasContext()
                ? this
                : new Path(newContext, getComponents());
    }

    /**
     * Returns a Path without any context.
     *
     * @return the Path with no context
     */
    public Path toPathWithoutContext() {
        final var context = getContext();
        final var components = getComponents();
        return context == null ? this : new Path(null, components);
    }

    /***
     * Returns a {@link Path} with the supplied {@link HasNodeId}, throwing an exception if the supplied
     * {@link HasNodeId} cannot produce a {@link NodeId}.
     *
     * @param hasNodeId the {@link HasNodeId} instance
     * @return a new {@link Path} with the {@link NodeId} context
     */
    public Path toPathWithNodeId(final HasNodeId hasNodeId) {
        return toPathWithContext(hasNodeId.getNodeId().asString());
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
     * Gets a byte[] representation of this {@link Path}.
     *
     * @return the byte array
     */
    public byte[] toByteArray() {
        return toNormalizedPathString().getBytes(ENCODING);
    }

    /**
     * Converts this {@link Path} to a {@link ByteBuffer}.
     *
     * @return this, as a byte buffer
     */
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toByteArray()).clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Objects.equals(getContextAndComponents(), path.getContextAndComponents());
    }

    @Override
    public int hashCode() {
        return getContextAndComponents().hashCode();
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

    /**
     * Gets a path from a {@link ByteBuffer}
     * @param byteBuffer the {@link ByteBuffer}
     * @return the {@link Path}
     */
    public static Path fromByteBuffer(final ByteBuffer byteBuffer) {
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
     * Checks if two paths match.
     *
     * @param lhs
     * @param rhs
     * @return
     */
    public static boolean matches(final Path lhs, final Path rhs) {

        final BiPredicate<String, String> matches = (l, r) ->
                WILDCARD.equals(l) ||
                WILDCARD.equals(r) ||
                Objects.equals(l, r);

        if (matches.test(lhs.getContext(), rhs.getContext())) {

            final var lhsComponents = lhs.getComponents();
            final var rhsComponents = rhs.getComponents();
            final var lhsLimit = lhs.isWildcardRecursive() ? lhsComponents.size() - 1 : lhsComponents.size();
            final var rhsLimit = rhs.isWildcardRecursive() ? rhsComponents.size() - 1 : rhsComponents.size();

            final var limit = min(lhsLimit, rhsLimit);
            final var lhsItr = lhsComponents.iterator();
            final var rhsItr = rhsComponents.iterator();

            var match = true;

            for (int count = 0; match && count < limit && lhsItr.hasNext() && rhsItr.hasNext(); ++count) {
                final var lhsComponent = lhsItr.next();
                final var rhsComponent = rhsItr.next();
                match = matches.test(lhsComponent, rhsComponent);
            }

            return match;

        } else {
            return false;
        }

    }

    public static ContextAndComponents contextAndComponentsFromPath(
            final String path,
            final String pathSeparator) {

        final var componentSplitPattern = quote(pathSeparator);

        if (!path.contains(CONTEXT_SEPARATOR)) {

            final List<String> components = Stream.of(path.split(componentSplitPattern))
                    .map(String::trim)
                    .filter(c -> !c.isEmpty())
                    .collect(toList());

            return new ContextAndComponents(null, components);

        }

        final var contextAndPath = Stream.of(CONTEXT_SPLIT_PATTERN.split(path))
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .collect(toList());

        if (contextAndPath.size() != 2) {
            throw new IllegalArgumentException("Expected two results when splitting path with '://': " + path);
        }

        final var context = contextAndPath.get(0);
        final var componentsString = contextAndPath.get(1);

        final List<String> components = Stream.of(componentsString.split(componentSplitPattern))
                .map(String::trim)
                .filter(c -> ! c.isEmpty())
                .collect(toList());

        return new ContextAndComponents(context, components);

    }

    /**
     * The raw data structure which backs the {@link Path} type.
     */
    public static final class ContextAndComponents implements Serializable {

        private final String context;
        private final List<String> components;

        private ContextAndComponents() {
            context = null;
            components = emptyList();
        }

        public ContextAndComponents(final String context, final List<String> components) {

            this.context = context == null
                    ? null
                    : context.isBlank()
                    ? null
                    : context.strip();

            this.components = copyOf(components);

        }

        public String getContext() {
            return context;
        }

        public List<String> getComponents() {
            return components;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContextAndComponents that = (ContextAndComponents) o;
            return Objects.equals(getContext(), that.getContext()) && Objects.equals(getComponents(), that.getComponents());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getContext(), getComponents());
        }

        public String getComponent(final int index) {
            return components.get(index < 0
                    ? components.size() + index
                    : index
            );
        }

    }

}
