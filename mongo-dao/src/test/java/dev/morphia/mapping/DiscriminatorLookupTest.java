package dev.morphia.mapping;

import org.testng.annotations.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

/**
 * Regression tests for #104: the name-keyed discriminator cache must not silently pin the
 * {@link Class} of a superseded classloader generation after an element reload.
 *
 * <p>Scenario: an element reload builds a fresh classloader generation (new loader) that defines
 * the <em>same</em> fully-qualified class name. A {@link DiscriminatorLookup} seeded against the
 * old generation must not keep returning the superseded {@link Class}, since freshly reloaded
 * element code that receives an instance of the old generation's class will throw a
 * {@link ClassCastException} (the two classes have the same name but differ by classloader).
 * The reported failure self-resolves only on a full JVM restart, which is consistent with the
 * pinned class living in process-scoped, name-keyed state.
 */
public class DiscriminatorLookupTest {

    private static final String DISCRIMINATOR =
            "dev.morphia.mapping.DiscriminatorLookupTest$GenerationEntity";

    @Test
    public void lookup_resolvesThroughTheCurrentContextClassLoader() throws Exception {
        final var lookup = new DiscriminatorLookup();
        final var loader = urlClassLoaderOverTestClasses();
        final var previousTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            assertSame(lookup.lookup(DISCRIMINATOR), Class.forName(DISCRIMINATOR, true, loader));
        } finally {
            Thread.currentThread().setContextClassLoader(previousTCCL);
            loader.close();
        }
    }

    @Test
    public void lookup_afterReload_returnsTheNewGenerationNotThePinnedPrevious() throws Exception {
        final var lookup = new DiscriminatorLookup();
        final var loader1 = urlClassLoaderOverTestClasses();
        final var loader2 = urlClassLoaderOverTestClasses();
        final var previousTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader1);
            final var generation1 = Class.forName(DISCRIMINATOR, true, loader1);
            assertSame(lookup.lookup(DISCRIMINATOR), generation1);

            // Reload: a fresh element generation defines the same FQN under a new loader.
            Thread.currentThread().setContextClassLoader(loader2);
            final var generation2 = Class.forName(DISCRIMINATOR, true, loader2);
            assertNotSame(generation1, generation2);

            // The cache must not hand the pinned generation-1 class to generation-2 code.
            assertSame(lookup.lookup(DISCRIMINATOR), generation2);
        } finally {
            Thread.currentThread().setContextClassLoader(previousTCCL);
            loader1.close();
            loader2.close();
        }
    }

    @Test
    public void lookup_cachedClassSurvivesWhenCurrentContextCannotSeeIt() throws Exception {
        final var previousTCCL = Thread.currentThread().getContextClassLoader();
        // Build the lookup under a blind TCCL so its captured platform fallback loader cannot see
        // element classes (mirrors production, where the platform loader cannot see element code).
        final var platformLoader = new URLClassLoader(new URL[0], null);
        try {
            Thread.currentThread().setContextClassLoader(platformLoader);
            final var lookup = new DiscriminatorLookup();

            final var loader1 = urlClassLoaderOverTestClasses();
            Thread.currentThread().setContextClassLoader(loader1);
            final var generation1 = Class.forName(DISCRIMINATOR, true, loader1);
            assertSame(lookup.lookup(DISCRIMINATOR), generation1);

            // A blind, platform-like context that cannot see the element class retains the cache.
            Thread.currentThread().setContextClassLoader(platformLoader);
            assertSame(lookup.lookup(DISCRIMINATOR), generation1);
        } finally {
            Thread.currentThread().setContextClassLoader(previousTCCL);
            platformLoader.close();
        }
    }

    private static URLClassLoader urlClassLoaderOverTestClasses() throws Exception {
        final URL location = DiscriminatorLookupTest.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();
        return new URLClassLoader(new URL[]{location}, null);
    }

    /** A trivial class whose simple form can be independently defined by any classloader. */
    private static class GenerationEntity {}

}