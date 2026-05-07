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
 * <p>All other behaviour — including the {@code discriminatorClassMap} cache, duplicate-discriminator
 * detection, and {@link #searchPackages} fallback — is identical to the Morphia original.
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
        if (discriminatorClassMap.containsKey(discriminator)) {
            return discriminatorClassMap.get(discriminator);
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
