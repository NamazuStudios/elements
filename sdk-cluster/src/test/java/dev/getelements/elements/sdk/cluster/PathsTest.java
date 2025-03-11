package dev.getelements.elements.sdk.cluster;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.path.Paths;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static dev.getelements.elements.sdk.cluster.path.Path.formatPath;
import static java.lang.String.format;
import static java.util.Collections.shuffle;
import static java.util.List.copyOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class PathsTest {

    @Test
    public void testSortFirst() {

        final var list = new ArrayList<Path>();

        final var recursiveBar = formatPath("/bar/**");
        list.add(recursiveBar);

        final var recursiveFoo = formatPath("/foo/**");
        list.add(recursiveFoo);

        final var topWildcard = formatPath("/foo/*/bar/*");
        list.add(topWildcard);

        for (int i = 0; i < 5; ++i) {

            final var wildcard = formatPath("/foo/%d/bar/*", i);
            list.add(wildcard);

            final var recursiveWildcard = formatPath("/foo/%d/bar/**", i);
            list.add(recursiveWildcard);

            for (int j = 0; j < 5; ++j) {
                final var path = formatPath("/foo/%d/bar/%d", i, j);
                list.add(path);
            }

        }

        final var original = copyOf(list);
        shuffle(list);
        list.sort(Paths.WILDCARD_FIRST);
        assertEquals(list, original);

    }

    @Test
    public void testSortLast() {

        final var list = new ArrayList<Path>();

        final var recursiveBar = formatPath("/bar/**");
        list.add(recursiveBar);

        final var recursiveFoo = formatPath("/foo/**");
        list.add(recursiveFoo);

        for (int i = 0; i < 5; ++i) {

            for (int j = 0; j < 5; ++j) {
                final var path = formatPath("/foo/%d/bar/%d", i, j);
                list.add(path);
            }

            final var wildcard = formatPath("/foo/%d/bar/*", i);
            list.add(wildcard);

            final var recursiveWildcard = formatPath("/foo/%d/bar/**", i);
            list.add(recursiveWildcard);

        }

        final var bottomWildcard = formatPath("/foo/*/bar/*");
        list.add(bottomWildcard);

        final var original = copyOf(list);
        shuffle(list);
        list.sort(Paths.WILDCARD_LAST);
        assertEquals(list, original);

    }

    @Test
    public void testSortedSetFirst() {

        final var list = new ArrayList<Path>();

        final var recursiveBar = formatPath("/bar/**");
        list.add(recursiveBar);

        final var recursiveFoo = formatPath("/foo/**");
        list.add(recursiveFoo);

        final var topWildcard = formatPath("/foo/*/bar/*");
        list.add(topWildcard);

        for (int i = 0; i < 5; ++i) {

            final var wildcard = formatPath("/foo/%d/bar/*", i);
            list.add(wildcard);

            final var recursiveWildcard = formatPath("/foo/%d/bar/**", i);
            list.add(recursiveWildcard);

            for (int j = 0; j < 5; ++j) {
                final var path = formatPath("/foo/%d/bar/%d", i, j);
                list.add(path);
            }

        }

        for (int i = 0; i < 5; ++i) {

            final var wildcard = formatPath("test://foo/%d/bar/*", i);
            list.add(wildcard);

            final var recursiveWildcard = formatPath("test://foo/%d/bar/**", i);
            list.add(recursiveWildcard);

            for (int j = 0; j < 5; ++j) {
                final var path = formatPath("test://foo/%d/bar/%d", i, j);
                list.add(path);
            }

        }

        final var original = copyOf(list);
        shuffle(list);

        final var treeSet = new TreeSet<>(Paths.WILDCARD_FIRST);
        treeSet.addAll(list);
        list.clear();
        list.addAll(treeSet);

        assertEquals(list, original);

    }

    @Test
    public void testSortedSetLast() {

        final var list = new ArrayList<Path>();

        final var recursiveBar = formatPath("/bar/**");
        list.add(recursiveBar);

        final var recursiveFoo = formatPath("/foo/**");
        list.add(recursiveFoo);

        for (int i = 0; i < 5; ++i) {

            for (int j = 0; j < 5; ++j) {
                final var path = formatPath("/foo/%d/bar/%d", i, j);
                list.add(path);
            }

            final var wildcard = formatPath("/foo/%d/bar/*", i);
            list.add(wildcard);

            final var recursiveWildcard = formatPath("/foo/%d/bar/**", i);
            list.add(recursiveWildcard);

        }

        final var bottomWildcard = formatPath("/foo/*/bar/*");
        list.add(bottomWildcard);

        final var original = copyOf(list);
        shuffle(list);

        final var treeSet = new TreeSet<>(Paths.WILDCARD_LAST);
        treeSet.addAll(list);
        list.clear();
        list.addAll(treeSet);

        assertEquals(list, original);

    }

    @Test(invocationCount = 100)
    public void testIterateHierarchy() {

        final var path = Paths.randomPath();

        var current = path.contextRootPath();
        final var expectedList = new ArrayList<>();

        for (var component : path.getComponents()) {
            current = current.appendComponents(component);
            expectedList.add(current);
        }

        final var expectedIterator = expectedList.iterator();
        final var actualIterator = Paths.iterateIntermediateHierarchy(path).iterator();

        while (actualIterator.hasNext() && expectedIterator.hasNext()) {
            assertEquals(actualIterator.next(), expectedIterator.next());
        }

        assertFalse(actualIterator.hasNext());
        assertFalse(expectedIterator.hasNext());

    }

    @DataProvider
    public Object[][] mixedPaths() {
        return new Object[][] {

                new Object[] { new Path("/a") },
                new Object[] { new Path("/a/b") },
                new Object[] { new Path("/a/b/c") },
                new Object[] { new Path("/*") },
                new Object[] { new Path("/a/*") },
                new Object[] { new Path("/a/b/*") },
                new Object[] { new Path("/a") },
                new Object[] { new Path("/*/b") },
                new Object[] { new Path("/a/*/c") },
                new Object[] { new Path("/*/b/c") },
                new Object[] { new Path("/a/**") },
                new Object[] { new Path("/a/b/**") },
                new Object[] { new Path("/a/b/c/**") },
                new Object[] { new Path("/*/b/**") },
                new Object[] { new Path("/a/*/c/**") },
                new Object[] { new Path("/*/b/c/**") },

                new Object[] { new Path("*://a") },
                new Object[] { new Path("*://a/b") },
                new Object[] { new Path("*://a/b/c") },
                new Object[] { new Path("*://*") },
                new Object[] { new Path("*://a/*") },
                new Object[] { new Path("*://a/b/*") },
                new Object[] { new Path("*://a") },
                new Object[] { new Path("*://*/b") },
                new Object[] { new Path("*://a/*/c") },
                new Object[] { new Path("*://*/b/c") },
                new Object[] { new Path("*://a/**") },
                new Object[] { new Path("*://a/b/**") },
                new Object[] { new Path("*://a/b/c/**") },
                new Object[] { new Path("*://*/b/**") },
                new Object[] { new Path("*://a/*/c/**") },
                new Object[] { new Path("*://*/b/c/**") },

                new Object[] { new Path("test://a") },
                new Object[] { new Path("test://a/b") },
                new Object[] { new Path("test://a/b/c") },
                new Object[] { new Path("test://*") },
                new Object[] { new Path("test://a/*") },
                new Object[] { new Path("test://a/b/*") },
                new Object[] { new Path("test://a") },
                new Object[] { new Path("test://*/b") },
                new Object[] { new Path("test://a/*/c") },
                new Object[] { new Path("test://*/b/c") },
                new Object[] { new Path("test://a/**") },
                new Object[] { new Path("test://a/b/**") },
                new Object[] { new Path("test://a/b/c/**") },
                new Object[] { new Path("test://*/b/**") },
                new Object[] { new Path("test://a/*/c/**") },
                new Object[] { new Path("test://*/b/c/**") },

        };
    }

    @Test(dataProvider = "mixedPaths")
    public void testComparesToZeroWildcardLast(final Path path) {
        final int compare = Paths.WILDCARD_LAST.compare(path, path);
        assertEquals(compare, 0, format("Expected path to compare 0 - %s", path));
    }

    @Test(dataProvider = "mixedPaths")
    public void testComparesToZeroWildcardFirst(final Path path) {
        final int compare = Paths.WILDCARD_FIRST.compare(path, path);
        assertEquals(compare, 0, format("Expected path to compare 0 - %s", path));
    }

    @Test(dataProvider = "mixedPaths")
    public void testSetDoesNotDuplicate(final Path path) {
        final var set = new HashSet<Path>();
        testSetDoesNotDuplicate(path, set);
    }

    @Test(dataProvider = "mixedPaths")
    public void testSortedSetDoesNotDuplicateWildcardLast(final Path path) {
        final var set = new TreeSet<>(Paths.WILDCARD_LAST);
        testSetDoesNotDuplicate(path, set);
    }

    @Test(dataProvider = "mixedPaths")
    public void testSortedSetDoesNotDuplicateWildcardFirst(final Path path) {
        final var set = new TreeSet<>(Paths.WILDCARD_FIRST);
        testSetDoesNotDuplicate(path, set);
    }

    public void testSetDoesNotDuplicate(final Path path, final Set<Path> set) {

        for (int i = 0; i < 1000; ++i) {
            set.add(path);
        }

        assertEquals(set.size(), 1);

    }

}
