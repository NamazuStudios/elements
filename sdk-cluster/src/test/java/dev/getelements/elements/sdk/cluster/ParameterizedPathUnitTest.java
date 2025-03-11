package dev.getelements.elements.sdk.cluster;

import dev.getelements.elements.sdk.cluster.path.ParameterizedPath;
import dev.getelements.elements.sdk.cluster.path.Path;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Map;

import static org.testng.Assert.*;

public class ParameterizedPathUnitTest {

    @Test
    public void testExtract() {

        final Path path = new Path("/foo/bar/baz");
        final ParameterizedPath parameterizedPath = new ParameterizedPath("/foo/{id0}/{id1}");
        assertTrue(parameterizedPath.matches(path), "Paramterized path does not match path.");

        final Map<String, String> extracted = parameterizedPath.extract(path);
        assertEquals(extracted.get("id0"), "bar");
        assertEquals(extracted.get("id1"), "baz");

        final Iterator<String> keyIterator = extracted.keySet().iterator();
        assertEquals(keyIterator.next(), "id0");
        assertEquals(keyIterator.next(), "id1");
        assertFalse(keyIterator.hasNext(), "Expected end of data set.");

        final Iterator<String> valueIterator = extracted.values().iterator();
        assertEquals(valueIterator.next(), "bar");
        assertEquals(valueIterator.next(), "baz");
        assertFalse(valueIterator.hasNext(), "Expected end of data set.");

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExtractFails() {
        final Path path = new Path("/foo/bar/baz");
        final ParameterizedPath parameterizedPath = new ParameterizedPath("/foo/{id0}");
        parameterizedPath.extract(path);

    }

}
