package dev.getelements.elements.sdk.cluster;

import dev.getelements.elements.sdk.cluster.path.Path;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

import static dev.getelements.elements.sdk.cluster.path.Paths.randomPath;
import static org.testng.Assert.*;

public class PathTest {

    @Test
    public void testPathConstructNoContext() {
        Path path;

        path = new Path("/test/foo");
        assertEquals(path.getComponents().get(0),"test");
        assertEquals(path.getComponents().get(1),"foo");
        assertFalse(path.isWildcard());
        assertFalse(path.isWildcardRecursive());
        assertNull(path.getContext());

        path = new Path("/test/foo/*");
        assertEquals(path.getComponents().get(0),"test");
        assertEquals(path.getComponents().get(1),"foo");
        assertEquals(path.getComponents().get(2),"*");
        assertTrue(path.isWildcard());
        assertFalse(path.isWildcardRecursive());
        assertNull(path.getContext());

        path = new Path("/test/foo");
        assertEquals(path.getComponents().get(0),"test");
        assertEquals(path.getComponents().get(1),"foo");
        assertFalse(path.isWildcard());
        assertFalse(path.isWildcardRecursive());
        assertNull(path.getContext());

        path = new Path("/test/foo/*/bar/**");
        assertEquals(path.getComponents().get(0),"test");
        assertEquals(path.getComponents().get(1),"foo");
        assertEquals(path.getComponents().get(2),"*");
        assertEquals(path.getComponents().get(3),"bar");
        assertEquals(path.getComponents().get(4),"**");
        assertTrue(path.isWildcard());
        assertTrue(path.isWildcardRecursive());
        assertNull(path.getContext());

    }

    @Test
    public void testPathConstructWithContext() {

        Path path;

        path = new Path("test://test/foo");
        assertEquals(path.getComponents().get(0),"test");
        assertEquals(path.getComponents().get(1),"foo");
        assertFalse(path.isWildcard());
        assertFalse(path.isWildcardRecursive());
        assertEquals(path.getContext(), "test");

        path = new Path("test://test/foo/*");
        assertEquals(path.getComponents().get(0),"test");
        assertEquals(path.getComponents().get(1),"foo");
        assertEquals(path.getComponents().get(2),"*");
        assertTrue(path.isWildcard());
        assertFalse(path.isWildcardRecursive());
        assertEquals(path.getContext(), "test");
        assertEquals(path.getContext(), "test");

        path = new Path("test://test/foo");
        assertEquals(path.getComponents().get(0),"test");
        assertEquals(path.getComponents().get(1),"foo");
        assertFalse(path.isWildcard());
        assertFalse(path.isWildcardRecursive());
        assertEquals(path.getContext(), "test");
        assertEquals(path.getContext(), "test");

        path = new Path("test://test/foo/*/bar/**");
        assertEquals(path.getComponents().get(0),"test");
        assertEquals(path.getComponents().get(1),"foo");
        assertEquals(path.getComponents().get(2),"*");
        assertEquals(path.getComponents().get(3),"bar");
        assertEquals(path.getComponents().get(4),"**");
        assertTrue(path.isWildcard());
        assertTrue(path.isWildcardRecursive());
        assertEquals(path.getContext(), "test");

    }

    @DataProvider
    public static Object[][] matching() {
        return new Object[][] {
                new Object[] { new Path("/a/b/c"),       new Path("/a/b/c") },
                new Object[] { new Path("*://a/b/c"),    new Path("test://a/b/c") },
                new Object[] { new Path("test://a/b/c"), new Path("test://a/b/c") },
                new Object[] { new Path("test://a/b/*"), new Path("test://a/b/c") },
                new Object[] { new Path("test://**"),    new Path("test://a/b/c") },
                new Object[] { new Path("test://a/**"),  new Path("test://a/b") },
                new Object[] { new Path("test://a/**"),  new Path("test://a/b/c") },
        };
    }

    @Test(dataProvider = "matching")
    public void testMatches(final Path left, final Path right) {
        assertTrue(left.matches(right), "Left does not match right.");
        assertTrue(right.matches(left), "Right does not match left.");
        assertTrue(left.matches(left));
        assertTrue(right.matches(right));
    }

    @DataProvider
    public static Object[][] nonMatching() {
        return new Object[][] {
                new Object[] { new Path("test://a/b/c"), new Path("test://a/b/d") },
                new Object[] { new Path("test://a/b/c"), new Path("test://a/b/d/*") },
                new Object[] { new Path("test://a/**"), new Path("test://b/c") },
                new Object[] { new Path("test://a/**"), new Path("test://b/c/d") },
        };
    }

    @Test(dataProvider = "nonMatching")
    public void testNonMatches(final Path left, final Path right) {
        assertFalse(left.matches(right), "Left matches right.");
        assertFalse(right.matches(left), "Right matches left.");
        assertTrue(left.matches(left), "Left does not match right.");
        assertTrue(right.matches(right), "Right does not match left.");
    }

    @Test(invocationCount = 50)
    public void testEqualsAndHashCode() {
        final var seed = System.nanoTime();
        final var left = randomPath(seed);
        final var right = randomPath(seed);
        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
    }

    @Test(invocationCount = 50)
    public void testEqualsAndHashCodeNegative() {
        final var left = randomPath(System.nanoTime());
        final var right = randomPath(System.nanoTime());
        assertNotEquals(left, right);
    }

    @Test
    public void testAppendUuidToWildcard() {

        final var path = new Path("/test/foo/*");
        final var appended = path.appendUUIDIfWildcard();
        assertNotEquals(path, appended);

        final var uuidString = appended.getComponent(-1);
        final var uuid = UUID.fromString(uuidString);

        assertTrue(path.isWildcard());
        assertEquals(uuid.version(), 4);

    }

    @DataProvider
    public Object[][] getRelativePathsToTest() {
        return new Object[][] {
                new Object[] { new Path(), java.nio.file.Path.of("") },
                new Object[] { new Path("/foo"), java.nio.file.Path.of("foo") },
                new Object[] { new Path("/bar"), java.nio.file.Path.of("bar") },
                new Object[] { new Path("/foo/bar"), java.nio.file.Path.of("foo/bar") },
                new Object[] { new Path("/a/b/c/d/e"), java.nio.file.Path.of("a/b/c/d/e") },
        };
    }

    @Test(dataProvider = "getRelativePathsToTest")
    public void testToRelativePath(final Path rtPath, final java.nio.file.Path fsPath) {
        assertEquals(rtPath.toRelativeFilesystemPath(), fsPath);
    }

    @Test
    public void testToWildcardRecursive() {

        var plain = new Path("/foo");
        assertEquals(plain.toWildcardRecursive(), new Path("/foo/**"));

        var wildcard = new Path("/foo/*");
        assertEquals(wildcard.toWildcardRecursive(), new Path("/foo/**"));

        var wildcardRecursive = new Path("/foo/**");
        assertSame(wildcardRecursive.toWildcardRecursive(), wildcardRecursive);

    }

    @Test
    public void testToRelativePathString() {
        var path = new Path("/foo/bar");
        assertEquals(path.toRelativePathString("/"), "foo/bar");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testToRelativePathStringFail() {
        var path = new Path("foo:///foo/bar");
        path.toRelativePathString("/");
    }

    @Test
    public void testToPathWithContext() {
        final var bare = new Path("bar");
        assertEquals(new Path("foo://bar"), bare.toPathWithContext("foo"));
        final var withContext = new Path("foo://bar");
        assertEquals(new Path("bar://bar"), withContext.toPathWithContext("bar"));
    }

    @Test
    public void testToPathWithContextIfAbsent() {
        final var withContext = new Path("foo://bar");
        assertSame(withContext, withContext.toPathWithContextIfAbsent("foo"));
        assertSame(withContext, withContext.toPathWithContextIfAbsent("bar"));
    }

}
