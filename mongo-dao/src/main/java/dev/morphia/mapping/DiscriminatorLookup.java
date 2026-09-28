package dev.morphia.mapping;

import dev.morphia.mapping.codec.pojo.EntityModel;
import dev.morphia.sofia.Sofia;
import org.bson.codecs.configuration.CodecConfigurationException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Patched shadow of {@code dev.morphia.mapping.DiscriminatorLookup} from morphia-core.
 *
 * <p>This class replaces Morphia's {@code DiscriminatorLookup} on the classpath via the standard
 * JVM classpath-order rule: because {@code mongo-dao.jar} is listed before {@code morphia-core.jar}
 * in the application classpath, the JVM loads this version instead of Morphia's.
 *
 * <p>The only behavioral change from the Morphia 2.4.x original is in {@link #getClassForName}:
 * the original uses {@code Class.forName(name, true, this.classLoader)} where
 * {@code this.classLoader} is the thread context classloader captured <em>at construction time</em>
 * (i.e. the platform/application classloader, which cannot see element-specific classes).
 *
 * <p>Our replacement tries the <em>current</em> thread context classloader first, then falls back
 * to the captured classloader.  This allows element-owned {@code @Entity} classes to be resolved
 * when the TCCL has been set to the element's isolated classloader — either by
 * {@code ClassLoaderSwitchHandler} during HTTP request dispatch, or by {@code JakartaRsLoader}
 * during element startup (Jersey initialization).
 *
 * <p>A second, unrelated behavioral change exists in {@link #lookup}: the
 * {@code discriminatorClassMap} cache is generation-aware.  The Morphia original returns the
 * cached {@link Class} unconditionally, which silently pins the class of a superseded
 * classloader generation after an element reload (both generations share the same
 * fully-qualified name but differ by classloader, producing {@link ClassCastException}s in
 * freshly reloaded element code).  {@link #lookup} now re-resolves through the <em>current</em>
 * thread context classloader whenever the cached entry was defined by a different loader, and
 * swaps in the current generation's class.  If the current context cannot see the class at all,
 * the cached entry is retained so calls from platform/decoder threads never regress into hard
 * failures.
 *
 * <p>All other behaviour — including duplicate-discriminator detection and
 * {@link #searchPackages} fallback — is identical to the Morphia original.
 */
public final class DiscriminatorLookup {

    private final Map<String, Class<?>> discriminatorClassMap = new ConcurrentHashMap<>();
    private final Set<String> packages = new ConcurrentSkipListSet<>();
    private final ClassLoader classLoader;

    public DiscriminatorLookup() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public void addModel(final EntityModel entityModel) {
        final var extant = (Class<?>) discriminatorClassMap.put(
                entityModel.getDiscriminator(), entityModel.getType());
        if (extant != null) {
            throw new MappingException(Sofia.duplicateDiscriminators(
                    entityModel.getDiscriminator(),
                    extant.getName(),
                    entityModel.getType().getName()));
        }
    }

    public Class<?> lookup(final String discriminator) {
        final var cached = (Class<?>) discriminatorClassMap.get(discriminator);
        if (cached != null) {
            return isCurrentGeneration(cached)
                    ? cached
                    : resolveAndSwap(discriminator, cached);
        }
        var clazz = getClassForName(discriminator);
        if (clazz == null) {
            clazz = searchPackages(discriminator);
        }
        if (clazz == null) {
            throw new CodecConfigurationException(
                    String.format("A class could not be found for the discriminator: '%s'.", discriminator));
        }
        discriminatorClassMap.put(discriminator, clazz);
        return clazz;
    }

    /**
     * Returns {@code true} if the cached {@link Class} was defined by the current thread context
     * classloader (or by the platform classloader on a platform thread), so it is not a superseded
     * element generation.
     */
    private boolean isCurrentGeneration(final Class<?> cached) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return tccl == cached.getClassLoader() || (tccl == classLoader && cached.getClassLoader() == classLoader);
    }

    /**
     * Re-resolves the discriminator through the current thread context classloader and swaps in the
     * current generation's {@link Class}, falling back to (and retaining) the cached entry when the
     * current context cannot see the class.
     */
    private Class<?> resolveAndSwap(final String discriminator, final Class<?> cached) {
        final var resolved = getClassForName(discriminator);
        if (resolved == null) {
            return cached;
        }
        discriminatorClassMap.put(discriminator, resolved);
        return resolved;
    }

    private Class<?> getClassForName(final String className) {
        // Try the current TCCL first.  During HTTP requests the TCCL is the element's isolated
        // classloader (set by ClassLoaderSwitchHandler).  During element startup (Jersey init)
        // it is set by JakartaRsLoader before calling classLoaderHandler.start().  Either way,
        // element-owned classes are found here without needing explicit EntityRegistry
        // registration.
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != classLoader) {
            try {
                return Class.forName(className, true, tccl);
            } catch (ClassNotFoundException ignored) {
            }
        }
        // Fall back to the classloader captured at construction time (platform classloader).
        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private Class<?> searchPackages(final String discriminator) {
        for (final String packageName : packages) {
            final var clazz = getClassForName(packageName + "." + discriminator);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

}
