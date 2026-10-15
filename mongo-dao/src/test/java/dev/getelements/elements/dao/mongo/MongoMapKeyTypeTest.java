package dev.getelements.elements.dao.mongo;

import dev.morphia.config.MorphiaConfig;
import dev.morphia.mapping.DiscriminatorFunction;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.codec.pojo.EntityModel;
import dev.morphia.mapping.codec.pojo.PropertyModel;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.testng.Assert.assertTrue;

/**
 * Asserts that no Morphia-mapped entity registers a map property keyed by an unsupported type. Morphia's
 * {@code MapKeyTypeConstraint} warns (or fails) on map keys that resolve to {@link Object} (e.g. raw or
 * {@code Document}-typed properties, see issue #105), so every map property in the mapped package must be
 * keyed by a supported type such as {@link String}.
 * <p>
 * Maps the same package and `MorphiaConfig` as {@link MorphiaConfigProvider} to reproduce the runtime mapping.
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
            UUID.class);

    @Test
    public void everyMappedMapIsKeyedBySupportedType() {

        final var config = MorphiaConfig.load()
                .applyIndexes(true)
                .enablePolymorphicQueries(true)
                .discriminator(DiscriminatorFunction.className())
                .packages(List.of("dev.getelements.elements.dao.mongo.*"));

        final var mapper = new Mapper(config);
        config.packages().forEach(mapper::map);

        final var offenders = new ArrayList<String>();

        for (final EntityModel model : mapper.getMappedEntities()) {

            for (final PropertyModel pm : model.getProperties()) {

                if (!pm.isMap()) continue;

                final var params = pm.getTypeData().getTypeParameters();
                final Class<?> key = params.isEmpty() ? null : params.get(0).getType();

                if (key == null || Object.class.equals(key)) {
                    offenders.add(model.getType().getName() + "#" + pm.getName()
                            + " maps keyed as Object; key it with a supported type instead (e.g. String)");
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

}