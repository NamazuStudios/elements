package dev.getelements.elements.sdk.cluster.path;

import dev.getelements.elements.sdk.cluster.id.exception.InvalidNodeIdException;
import dev.getelements.elements.sdk.cluster.id.HasNodeId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
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
 * Represents the path scheme for use in the server. This is used for routing and addressing resources within the
 * cluster. Paths are a universal way to specify remote network resources in a variety of ways. This utility class can
 * be used to parse the paths and structure the data. Additionally, it allows for parsing of separators and wildcards
 * with alternative representations if necessary.
 *
 * Additionally, This implements {@link HasNodeId} which uses the {@link #getContext()} to attempt to derive the
 * {@link NodeId} or throw an exception if the context does not produce a valid {@link NodeId}.
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
    @Deprecated
    public static final String WILDCARD_CONTEXT_REPRESENTATION = WILDCARD;

    /**
     * A {@link Pattern} to match valid path components. Excludes {@link #QUERY_SEPARATOR} and
     * {@link #FRAGMENT_SEPARATOR}, which are reserved delimiters at the {@link Path} string-grammar level.
     */
    public static final Pattern VALID_PATH_COMPONENT = Pattern.compile("[\\p{Print}&&[^?#]]+");

    /**
     * The separator introducing the query string.  Literal value "?", e.g. "myContext://foo/bar?baz=qux".
     */
    public static final String QUERY_SEPARATOR = "?";

    /**
     * The separator between individual query parameters.  Literal value "&amp;".
     */
    public static final String QUERY_PARAMETER_SEPARATOR = "&";

    /**
     * The separator between a query parameter's key and its value.  Literal value "=".
     */
    public static final String QUERY_KEY_VALUE_SEPARATOR = "=";

    /**
     * Reserved for a future fragment component.  Literal value "#".  Not currently parsed, but forbidden within
     * path components and query keys/values since it is a structurally reserved delimiter.
     */
    public static final String FRAGMENT_SEPARATOR = "#";

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
        this(new ContextAndComponents(null, emptyList(), Map.of()));
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
        this(parent.getContext(), components(parent, path),
                path.getParameters().isEmpty() ? parent.getParameters() : path.getParameters());
    }

    private static List<String> components(final Path parent, final Path path) {

        if (parent.hasContext() && path.hasContext() && !Objects.equals(path.getContext(), parent.getContext())) {
            throw new InvalidPathException("Parent path must match " + parent.getContext() + "!=" + path.getComponents());
        } if (!parent.hasContext() && path.hasContext()) {
            throw new InvalidPathException("Parent path must have context if child has context.");
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
        this (new ContextAndComponents(context, components, Map.of()));
    }

    /**
     * Creates a path with components, context, and query parameters, threading the parameters through without
     * exposing a new public constructor overload.
     */
    private Path(final String context, final List<String> components, final Map<String, List<String>> parameters) {
        this(new ContextAndComponents(context, components, parameters));
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
            throw new InvalidPathException("Wildcard recursive paths must end with " + WILDCARD_RECURSIVE);
        }

        this.wildcardIndices = IntStream.range(0,  components.size())
                .filter(i -> WILDCARD.equals(components.get(i)))
                .toArray();

        if (WILDCARD_RECURSIVE.equals(context)) {
            throw new InvalidPathException("Context cannot be: " + WILDCARD_RECURSIVE);
        }

        for (final var component : components) {

            if (component.contains(PATH_SEPARATOR)) {
                throw new InvalidPathException(component + " cannot contain separator.");
            }

            if (!VALID_PATH_COMPONENT.matcher(component).matches()) {
                throw new InvalidPathException(component + " has invalid characters");
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
        return new Path(getContext(), components, getParameters());
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
        return new Path(getContext(), components, getParameters());
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

        // NOTE: this pre-existing constructor call drops context (uses the 1-arg, context-less constructor),
        // which is unrelated to query-parameter support and left as-is; only parameter threading is added here.
        return new Path(null, components, getParameters());

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
        return components.isEmpty()
                ? this
                : new Path(context, components.subList(0, components.size() - 1), getParameters());
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
        return new Path(new ContextAndComponents(getContext(), emptyList(), Map.of()));
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
     * Gets the decoded values for the named query parameter, in the order they appeared.
     *
     * @param name the parameter name
     * @return the list of decoded values, never null, empty if the parameter is absent
     */
    public List<String> getParameter(final String name) {
        return getContextAndComponents().getParameter(name);
    }

    /**
     * Gets the first decoded value for the named query parameter.
     *
     * @param name the parameter name
     * @return the first decoded value, or null if the parameter is absent
     */
    public String getFirstParameter(final String name) {
        final var values = getParameter(name);
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * Gets all query parameters as an unmodifiable, insertion-ordered map of decoded values.
     *
     * @return the query parameters, never null, empty if this path has no query
     */
    public Map<String, List<String>> getParameters() {
        return getContextAndComponents().getParameters();
    }

    /**
     * @return true if this path has any query parameters
     */
    public boolean hasQuery() {
        return !getParameters().isEmpty();
    }

    /**
     * @param name the parameter name
     * @return true if the named query parameter is present, regardless of value
     */
    public boolean hasParameter(final String name) {
        return getParameters().containsKey(name);
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
        return new Path(getContext(), components, getParameters());

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
                ? new Path(context, components.subList(0, components.size() - 1), getParameters())
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
        final var base = context != null && shouldIncludeContext
                ? format("%s://%s", context, join(pathSeparator, components))
                : format("/%s", join(pathSeparator, components));
        final var query = encodeQueryString(getParameters());
        return query.isEmpty() ? base : base + QUERY_SEPARATOR + query;
    }

    /**
     * Returns a Path which will have the context specified.
     *
     * @param newContext the context
     * @return the Path, or this if the context matches
     * @throws InvalidPathException if the context mismatches
     */
    public Path toPathWithContext(final String newContext) {
        return Objects.equals(getContext(), newContext)
                ? this
                : new Path(newContext, getComponents(), getParameters());
    }

    /**
     * Returns a Path which will have the context specified.
     *
     * @param hasNodeId the {@link HasNodeId} instance
     * @return the Path, or this if the context matches
     * @throws InvalidPathException if the context mismatches
     */
    public Path toPathWithContextIfAbsent(final HasNodeId hasNodeId) {
        return hasContext()
                ? this
                : new Path(hasNodeId.getNodeId().toString(), getComponents(), getParameters());
    }

    /**
     * Returns a Path which will have the context specified.
     *
     * @param newContext the context
     * @return the Path, or this if the context matches
     * @throws InvalidPathException if the context mismatches
     */
    public Path toPathWithContextIfAbsent(final String newContext) {
        return hasContext()
                ? this
                : new Path(newContext, getComponents(), getParameters());
    }

    /**
     * Returns a Path without any context.
     *
     * @return the Path with no context
     */
    public Path toPathWithoutContext() {
        final var context = getContext();
        final var components = getComponents();
        return context == null ? this : new Path(null, components, getParameters());
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
    public Optional<NodeId> findNodeId() {
        return Optional.empty();
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

    /**
     * Converts this {@link Path} to a {@link URI}.  Since {@link Path} has no authority/host concept (unlike a
     * full RFC 3986 URI), the context maps to the URI scheme and every component maps to a path segment -
     * constructed as {@code scheme:/c1/c2?query} (a single slash, not {@code scheme://c1/c2}, since the latter
     * would cause {@link URI} to parse the first component as an authority/host).  Each component and the query
     * string are percent-encoded per {@link #percentEncode(String)}.
     *
     * @return the equivalent {@link URI}
     * @throws InvalidPathException if the resulting string is not a legal {@link URI}
     */
    public URI toURI() {

        final var context = getContext();

        final var encodedComponents = getComponents()
                .stream()
                .map(Path::percentEncode)
                .collect(toList());

        final var pathPart = "/" + join(PATH_SEPARATOR, encodedComponents);
        final var query = encodeQueryString(getParameters());
        final var queryPart = query.isEmpty() ? "" : QUERY_SEPARATOR + query;

        final var uriString = context == null
                ? pathPart + queryPart
                : context + ":" + pathPart + queryPart;

        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new InvalidPathException("Unable to convert Path to URI: " + this, e);
        }

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
            .findNodeId()
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
        return new Path(contextAndComponentsFromPath(pathString, pathSeparator));
    }

    /**
     * Converts the supplied {@link URI} to a {@link Path}.  The URI's scheme maps to the context (null for a
     * context-less/relative URI), and its path segments and query map to this {@link Path}'s components and
     * query parameters, respectively.  A {@link URI} with a non-null authority (e.g. a real network URI like
     * {@code http://example.com/foo}) has no {@link Path} equivalent and is rejected.
     *
     * @param uri the {@link URI} to convert
     * @return the equivalent {@link Path}
     * @throws InvalidPathException if the {@link URI} has a non-null authority
     */
    public static Path fromURI(final URI uri) {

        if (uri.getAuthority() != null) {
            throw new InvalidPathException("URI has an authority, incompatible with Path: " + uri);
        }

        final var context = uri.getScheme();
        final var rawPath = uri.getRawPath() == null ? "" : uri.getRawPath();

        final List<String> components = Stream.of(rawPath.split(PATH_SEPARATOR))
                .map(Path::percentDecode)
                .filter(c -> !c.isEmpty())
                .collect(toList());

        final var rawQuery = uri.getRawQuery();

        final Map<String, List<String>> parameters = rawQuery == null
                ? Map.of()
                : parseQueryString(rawQuery);

        return new Path(new ContextAndComponents(context, components, parameters));

    }

    /**
     * Implements the conventional valueOf method by invoking {@link #fromURI(URI)}.
     *
     * @param uri the {@link URI} to convert
     * @return the equivalent {@link Path}
     */
    public static Path valueOf(final URI uri) {
        return fromURI(uri);
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

        final var queryIndex = path.indexOf(QUERY_SEPARATOR);
        final var pathPortion = queryIndex < 0 ? path : path.substring(0, queryIndex);
        final var queryPortion = queryIndex < 0 ? null : path.substring(queryIndex + 1);

        if (queryPortion != null && queryPortion.contains(QUERY_SEPARATOR)) {
            throw new InvalidPathException("Path contains multiple '?' separators: " + path);
        }

        final Map<String, List<String>> parameters = queryPortion == null
                ? Map.of()
                : parseQueryString(queryPortion);

        final var componentSplitPattern = quote(pathSeparator);

        if (!pathPortion.contains(CONTEXT_SEPARATOR)) {

            final List<String> components = Stream.of(pathPortion.split(componentSplitPattern))
                    .map(String::trim)
                    .filter(c -> !c.isEmpty())
                    .collect(toList());

            return new ContextAndComponents(null, components, parameters);

        }

        final var contextAndPath = Stream.of(CONTEXT_SPLIT_PATTERN.split(pathPortion))
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .collect(toList());

        if (contextAndPath.size() != 2) {
            throw new InvalidPathException("Expected two results when splitting path with '://': " + path);
        }

        final var context = contextAndPath.get(0);
        final var componentsString = contextAndPath.get(1);

        final List<String> components = Stream.of(componentsString.split(componentSplitPattern))
                .map(String::trim)
                .filter(c -> ! c.isEmpty())
                .collect(toList());

        return new ContextAndComponents(context, components, parameters);

    }

    /**
     * Parses a raw (already percent-encoded) query string into decoded parameters, grouped by key in the order
     * they were first seen, preserving per-key value order.  A bare key with no {@link #QUERY_KEY_VALUE_SEPARATOR}
     * decodes to an empty-string value.
     *
     * @param rawQuery the raw, percent-encoded query string (without the leading {@link #QUERY_SEPARATOR})
     * @return the decoded parameters, never null
     * @throws InvalidPathException if the query string contains malformed percent-encoding or illegal characters
     */
    public static Map<String, List<String>> parseQueryString(final String rawQuery) {

        if (rawQuery == null || rawQuery.isEmpty()) {
            return Map.of();
        }

        final var result = new LinkedHashMap<String, List<String>>();

        for (final var pair : rawQuery.split(QUERY_PARAMETER_SEPARATOR, -1)) {

            if (pair.isEmpty()) {
                continue;
            }

            final var eq = pair.indexOf(QUERY_KEY_VALUE_SEPARATOR);
            final var rawKey = eq < 0 ? pair : pair.substring(0, eq);
            final var rawValue = eq < 0 ? "" : pair.substring(eq + 1);

            validateRawQueryComponent(rawKey, pair);
            validateRawQueryComponent(rawValue, pair);

            result.computeIfAbsent(percentDecode(rawKey), k -> new ArrayList<>()).add(percentDecode(rawValue));

        }

        final var immutable = new LinkedHashMap<String, List<String>>();
        result.forEach((k, v) -> immutable.put(k, List.copyOf(v)));
        return Collections.unmodifiableMap(immutable);

    }

    /**
     * A pragmatic (not byte-perfect RFC 3986 ABNF) character class for a raw, still-percent-encoded query key or
     * value: unreserved characters, sub-delims, and the characters our own {@link #percentEncode(String)} never
     * escapes, plus '%' itself (validated separately below for well-formedness).
     */
    private static final Pattern VALID_QUERY_CHAR = Pattern.compile("[A-Za-z0-9\\-._~!$'()*+,;:@/%]+");

    private static void validateRawQueryComponent(final String raw, final String originalPair) {

        if (!raw.isEmpty() && !VALID_QUERY_CHAR.matcher(raw).matches()) {
            throw new InvalidPathException("Invalid character(s) in query component: " + originalPair);
        }

        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == '%') {
                if (i + 2 >= raw.length()
                        || Character.digit(raw.charAt(i + 1), 16) < 0
                        || Character.digit(raw.charAt(i + 2), 16) < 0) {
                    throw new InvalidPathException("Malformed percent-encoding in query component: " + originalPair);
                }
            }
        }

    }

    /**
     * Encodes decoded query parameters into a canonical, percent-encoded query string (without the leading
     * {@link #QUERY_SEPARATOR}).
     *
     * @param parameters the decoded parameters
     * @return the encoded query string, empty if there are no parameters
     */
    public static String encodeQueryString(final Map<String, List<String>> parameters) {

        if (parameters == null || parameters.isEmpty()) {
            return "";
        }

        final var joiner = new StringJoiner(QUERY_PARAMETER_SEPARATOR);

        for (final var entry : parameters.entrySet()) {
            final var encodedKey = percentEncode(entry.getKey());
            for (final var value : entry.getValue()) {
                joiner.add(encodedKey + QUERY_KEY_VALUE_SEPARATOR + percentEncode(value));
            }
        }

        return joiner.toString();

    }

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    /**
     * Percent-encodes a single (decoded) string per strict RFC 3986 &sect;2.3: the unreserved set
     * ({@code A-Za-z0-9-._~}) passes through literally; everything else, including space, is percent-encoded as
     * uppercase-hex UTF-8 bytes.  Space is never encoded as '+'.
     */
    private static String percentEncode(final String raw) {
        final var bytes = raw.getBytes(ENCODING);
        final var sb = new StringBuilder(bytes.length);
        for (final byte b : bytes) {
            final int c = b & 0xFF;
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '.' || c == '_' || c == '~') {
                sb.append((char) c);
            } else {
                sb.append('%').append(HEX[(c >> 4) & 0xF]).append(HEX[c & 0xF]);
            }
        }
        return sb.toString();
    }

    /**
     * Percent-decodes a single raw string.  Only safe to call on input already validated by
     * {@link #validateRawQueryComponent(String, String)} (or produced by {@link java.net.URI}), since literal
     * (non-percent) characters are written as single bytes rather than re-encoded as UTF-8.
     */
    private static String percentDecode(final String raw) {
        final var bytes = new ByteArrayOutputStream(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            final char c = raw.charAt(i);
            if (c == '%') {
                final int hi = Character.digit(raw.charAt(i + 1), 16);
                final int lo = Character.digit(raw.charAt(i + 2), 16);
                bytes.write((hi << 4) | lo);
                i += 2;
            } else {
                bytes.write(c);
            }
        }
        return new String(bytes.toByteArray(), ENCODING);
    }

    /**
     * The raw data structure which backs the {@link Path} type.
     */
    public record ContextAndComponents(
            String context,
            List<String> components,
            Map<String, List<String>> parameters) implements Serializable {

        public ContextAndComponents {

            context = context == null
                    ? null
                    : context.isBlank()
                    ? null
                    : context.strip();

            components = copyOf(components);

            final var normalized = new LinkedHashMap<String, List<String>>();

            if (parameters != null) {
                parameters.forEach((k, v) -> normalized.put(k, List.copyOf(v)));
            }

            parameters = Collections.unmodifiableMap(normalized);

        }

        public String getContext() {
            return context;
        }

        public List<String> getComponents() {
            return components;
        }

        public String getComponent(final int index) {
            return components.get(index < 0
                    ? components.size() + index
                    : index
            );
        }

        public Map<String, List<String>> getParameters() {
            return parameters;
        }

        public List<String> getParameter(final String name) {
            return parameters.getOrDefault(name, List.of());
        }

    }

}
