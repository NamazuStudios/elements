package dev.getelements.elements.sdk.dao;

import java.util.List;

/**
 * SPI that element authors implement to declare their MongoDB entity classes to the platform.
 *
 * <p>When an element exposes an implementation of this interface via
 * {@code @ElementServiceImplementation} + {@code @ElementServiceExport(EntityRegistry.class)},
 * the platform discovers it during element loading and pre-registers the declared classes with
 * {@code Mapper} — in the correct classloader context — before the element's REST
 * endpoint starts serving requests. This eliminates the need for {@code useDiscriminator = false}
 * on element-owned {@code @Entity} classes.
 *
 * <p>Example (Kotlin):
 * <pre>{@code
 * @ElementServiceImplementation
 * @ElementServiceExport(EntityRegistry::class)
 * class MyEntityRegistry : EntityRegistry {
 *     override fun entityClasses() = listOf(
 *         DocumentA::class.java,
 *         DocumentB::class.java
 *     )
 * }
 * }</pre>
 */
public interface EntityRegistry {

    /**
     * Returns the {@code @Entity} classes owned by this element.
     *
     * @return list of entity classes to register; must not be {@code null}
     */
    List<Class<?>> entityClasses();

}
