package dev.getelements.elements.dao.mongo;

import com.mongodb.client.MongoClients;
import dev.morphia.Morphia;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.config.MorphiaConfig;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.codec.pojo.EntityModel;
import dev.morphia.mapping.codec.pojo.PropertyModel;
import dev.morphia.mapping.codec.pojo.TypeData;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarFile;

import static org.testng.Assert.assertTrue;

/**
 * Asserts that no Morphia-mapped entity registers a map property keyed by an unsupported type. Morphia's
 * {@code MapKeyTypeConstraint} warns (or fails) on map keys that resolve to {@link Object} (e.g. raw or
 * {@code Document}-typed properties, see issue #105), so every map property in the mapped package must be
 * keyed by a supported type such as {@link String}.
 */
public class MongoMapKeyTypeTest {

    private static final Set<Class<?>> PRIMITIVE_LIKE = Set.of(
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Double.class, double.class,
            Float.class, float.class,
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            String.class,
            Date.class,
            Locale.class,
            Class.class,
            UUID.class,
            URI.class);

    @Test
    public void everyMappedMapIsKeyedBySupportedType() throws Exception {

        final var config = MorphiaConfig.load()
                .applyIndexes(true)
                .enablePolymorphicQueries(true)
                .discriminator(DiscriminatorFunction.className())
                .packages(List.of("dev.getelements.elements.dao.mongo.*"));

        final var mapper = Morphia.createDatastore(MongoClients.create(), config).getMapper();
        final var offenders = new ArrayList<String>();

        for (final Class<?> clazz : scanMappedPackageClasses()) {

            if (!clazz.isAnnotationPresent(Entity.class) && !clazz.isAnnotationPresent(Embedded.class)) {
                continue;
            }

            final EntityModel model;
            try {
                model = mapper.getEntityModel(clazz);
            } catch (final Exception ex) {
                offenders.add(clazz.getName() + " -> failed to map: " + ex.getMessage());
                continue;
            }

            for (final PropertyModel pm : model.getProperties()) {
                if (!pm.isMap()) continue;
                final var params = pm.getTypeData().getTypeParameters();
                final Class<?> key = params.isEmpty() ? null : params.get(0).getType();
                if (key == null || Object.class.equals(key)) {
                    offenders.add(model.getType().getName() + "#" + pm.getName()
                            + " maps keyed as Object; key with a supported type instead (e.g. String)");
                } else if (!isPrimitiveLike(key)) {
                    offenders.add(model.getType().getName() + "#" + pm.getName()
                            + " maps keyed by unsupported type: " + key.getName());
                }
            }

        }

        assertTrue(
                offenders.isEmpty(),
                "Morphia map key type violations: " + String.join("; ", offenders)
        );

    }

    private static boolean isPrimitiveLike(final Class<?> type) {
        return PRIMITIVE_LIKE.contains(type) || type.isEnum();
    }

    private static List<Class<?>> scanMappedPackageClasses() throws Exception {

        final var result = new ArrayList<Class<?>>();
        final var classpath = System.getProperty("java.class.path", "");

        for (final String element : classpath.split(File.pathSeparator)) {
            final var path = Path.of(element);
            if (!Files.exists(path)) continue;
            if (Files.isDirectory(path)) {
                scanDirectory(path, path, result);
            } else if (element.endsWith(".jar")) {
                scanJar(path, result);
            }
        }

        return result.stream().distinct().toList();

    }

    private static void scanDirectory(final Path root, final Path dir, final List<Class<?>> output) throws Exception {

        try (final var stream = Files.list(dir)) {
            for (final Path child : stream.toList()) {
                if (Files.isDirectory(child)) {
                    scanDirectory(root, child, output);
                } else if (child.toString().endsWith(".class") && child.toString().contains("dev/getelements/elements/dao/mongo")) {
                    final var className = root.relativize(child)
                            .toString()
                            .replace(File.separatorChar, '.')
                            .replace('/', '.')
                            .replace(".class", "");
                    addClass(output, className);
                }
            }
        }

    }

    private static void scanJar(final Path jarPath, final List<Class<?>> output) throws Exception {

        try (final var jar = new JarFile(jarPath.toFile())) {
            final var entries = jar.entries();
            while (entries.hasMoreElements()) {
                final var entry = entries.nextElement();
                final var name = entry.getName();
                if (name.endsWith(".class") && name.contains("dev/getelements/elements/dao/mongo")) {
                    addClass(output, name.replace('/', '.').replace(".class", ""));
                }
            }
        }

    }

    private static void addClass(final List<Class<?>> output, final String className) {
        if (className.contains("$") || className.endsWith(".package-info")) return;
        try {
            output.add(Class.forName(className, false, MongoMapKeyTypeTest.class.getClassLoader()));
        } catch (final ClassNotFoundException | LinkageError ignored) {
        }
    }

}