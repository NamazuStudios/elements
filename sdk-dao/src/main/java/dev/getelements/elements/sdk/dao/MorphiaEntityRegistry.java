package dev.getelements.elements.sdk.dao;

import java.util.List;

/**
 * SPI that element authors implement to declare their MongoDB entity classes to the platform.
 *
 * <p>When an element exposes an implementation of this interface via
 * {@code @ElementServiceImplementation} + {@code @ElementServiceExport(MorphiaEntityRegistry.class)},
 * the platform discovers it during element loading and pre-registers the declared classes with
 * Morphia's {@code Mapper} — in the correct classloader context — before the element's REST
 * endpoint starts serving requests. This eliminates the need for {@code useDiscriminator = false}
 * on element-owned {@code @Entity} classes.
 *
 * <p>Example (Kotlin):
 * <pre>{@code
 * @ElementServiceImplementation
 * @ElementServiceExport(MorphiaEntityRegistry::class)
 * class MyEntityRegistry : MorphiaEntityRegistry {
 *     override fun entityClasses() = listOf(
 *         OrgDocument::class.java,
 *         OrgInviteDocument::class.java
 *     )
 * }
 * }</pre>
 */
public interface MorphiaEntityRegistry {

    /**
     * Returns the Morphia {@code @Entity} classes owned by this element.
     *
     * @return list of entity classes to register; must not be {@code null}
     */
    List<Class<?>> entityClasses();

}
