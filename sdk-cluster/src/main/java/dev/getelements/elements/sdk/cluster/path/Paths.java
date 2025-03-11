package dev.getelements.elements.sdk.cluster.path;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.cluster.path.Path.WILDCARD;
import static dev.getelements.elements.sdk.cluster.path.Path.WILDCARD_RECURSIVE;

public class Paths {

    private Paths() {}

    /**
     * Static instance of {@link WildcardLast}
     */
    public static Comparator<Path> WILDCARD_LAST = new WildcardLast();

    /**
     * Sorts all {@link Path} instances with paths containing a wildcard appear last in the sort order.
     */
    public static class WildcardLast implements Comparator<Path> {
        @Override
        public int compare(final Path lhs, final Path rhs) {
            return comparePaths(lhs, rhs,
                    (lhsComponent, rhsComponent) ->
                        WILDCARD.equals(lhsComponent) && WILDCARD_RECURSIVE.equals(rhsComponent) ? -1 :
                        WILDCARD.equals(rhsComponent) && WILDCARD_RECURSIVE.equals(lhsComponent) ?  1 :
                        WILDCARD_RECURSIVE.equals(lhsComponent) && WILDCARD_RECURSIVE.equals(rhsComponent) ? 0 :
                        WILDCARD_RECURSIVE.equals(lhsComponent) ?  1 :
                        WILDCARD_RECURSIVE.equals(rhsComponent) ? -1 :
                        WILDCARD.equals(lhsComponent) && WILDCARD.equals(rhsComponent) ? 0 :
                        WILDCARD.equals(lhsComponent) ?  1 :
                        WILDCARD.equals(rhsComponent) ? -1 :
                        lhsComponent.compareTo(rhsComponent)
            );
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && getClass().equals(obj.getClass());
        }

    }

    /**
     * Static instance of {@link WildcardFirst}
     */
    public static Comparator<Path> WILDCARD_FIRST = new WildcardFirst();

    /**
     * Sorts all {@link Path} instances with paths containing a wildcard appear first in the sort order.
     */
    public static class WildcardFirst implements Comparator<Path> {
        @Override
        public int compare(final Path lhs, final Path rhs) {
            return comparePaths(
                    lhs, rhs,
                    (lhsComponent, rhsComponent) ->
                            WILDCARD.equals(lhsComponent) && WILDCARD_RECURSIVE.equals(rhsComponent) ? -1 :
                            WILDCARD.equals(rhsComponent) && WILDCARD_RECURSIVE.equals(lhsComponent) ?  1 :
                            WILDCARD_RECURSIVE.equals(lhsComponent) && WILDCARD_RECURSIVE.equals(rhsComponent) ? 0 :
                            WILDCARD_RECURSIVE.equals(lhsComponent) ? -1 :
                            WILDCARD_RECURSIVE.equals(rhsComponent) ?  1 :
                            WILDCARD.equals(lhsComponent) && WILDCARD.equals(rhsComponent) ? 0 :
                            WILDCARD.equals(lhsComponent) ? -1 :
                            WILDCARD.equals(rhsComponent) ?  1 :
                            lhsComponent.compareTo(rhsComponent)
            );
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && getClass().equals(obj.getClass());
        }

    }

    private static int comparePaths(final Path lhs, final Path rhs,
                                    final Comparator<String> componentComparator) {
        if (Objects.equals(lhs.getContext(), rhs.getContext())) {
            final var lhsComponents = lhs.getComponents();
            final var rhsComponents = rhs.getComponents();
            return compareComponents(lhsComponents, rhsComponents, componentComparator);
        } else if (lhs.getContext() == null) {
            return -1;
        } else if (rhs.getContext() == null) {
            return 1;
        } else {
            final var lhsContext = lhs.getContext();
            final var rhsContext = rhs.getContext();
            return componentComparator.compare(lhsContext, rhsContext);
        }
    }

    private static int compareComponents(final List<String> lhs, final List<String> rhs,
                                         final Comparator<String> componentComparator) {

        if (lhs.size() != rhs.size()) {
            return lhs.size() - rhs.size();
        }

        int result = 0;

        final var lhsItr = lhs.iterator();
        final var rhsItr = rhs.iterator();

        // We iterate as long as all components are the same.

        for (int count = 0; result == 0 && lhsItr.hasNext() && rhsItr.hasNext(); ++count) {
            final var lhsComponent = lhsItr.next();
            final var rhsComponent = rhsItr.next();
            result = componentComparator.compare(lhsComponent, rhsComponent);
        }

        // Finally if we end up with a zero-result (all components are the same)
        // but there are remaining components we will compare based on the size.

        return result;

    }

    /**
     * Iterates the supplied path descending into its hierarchy. This does not include the root path.
     *
     * @param path the path
     * @return an {@link Iterable<Path>} for all paths in the hierarchy
     */
    public static Iterable<Path> iterateIntermediateHierarchy(final Path path) {
        return () -> new Iterator<>() {

            Path current = path.contextRootPath();

            final Iterator<String> componentIterator = path.getComponents().iterator();

            @Override
            public boolean hasNext() {
                return componentIterator.hasNext();
            }

            @Override
            public Path next() {
                final var component = componentIterator.next();
                return current = current.appendComponents(component);
            }

        };
    }

    /**
     * Generates a random {@link Path}. Typically used for testing. This does not use SecureRandom.
     *
     * @return a random path
     */
    public static Path randomPath() {
        return randomPath(System.nanoTime());
    }

    /**
     * Generates a random {@link Path}. Typically used for testing. This does not use SecureRandom.
     *
     * @return a random path
     */
    public static Path randomPath(long seed) {
        return randomPath(new Random(seed),"abcdefghijklmnopqrstuvwxyz", 10, 20);
    }

    /**
     * Generates a random {@link Path}. Typically used for testing, but could also be used to generate paths as uuids.
     * Allows for the specification of the {@link Random} instance.
     *
     * @return a random path
     */
    public static Path randomPath(final Random random, final String letters, int maxComponents, int maxComponentLength) {

        final IntFunction<String> wordGenerator = componentLength -> random
                .ints(componentLength, 0, letters.length())
                .mapToObj(letterIndex -> letters.substring(letterIndex, letterIndex + 1))
                .reduce("", String::concat);

        final var context = wordGenerator.apply(random.nextInt(maxComponentLength));

        final var componentCount = random.nextInt(maxComponents);

        final var components = random.ints(componentCount, 1, 30)
                .mapToObj(wordGenerator)
                .collect(Collectors.toList());

        return new Path(context, components);

    }

}
