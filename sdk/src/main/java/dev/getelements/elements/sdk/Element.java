package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents an instance of an SDK Element, similar to a java {@link Element} but with more flexibility at the expense
 * of the rigid encapsulation rules. An {@link Element} has the following properties:
 *
 * <ul>
 *     <li>A unique name, which can be inherited from the package name.</li>
 *     <li>A set of exposed or provided services, which may or may not explicitly be defined in the Element itself</li>
 *     <li>Its own {@link ClassLoader} which isolates it from the rest of the classpath (with some exceptions)</li>
 *     <li>A set of properties, defined by {@link Attributes}</li>
 *     <li>A {@link ServiceLocator} which exposes its internally managed services and manages their lifecycle</li>
 * </ul>
 *
 * It is not intended to be a direct replacement for dependency injection or inversion of control, however it has some
 * features. Internally, an {@link Element} may be implemented using those. In fact, the default implementation
 * uses Guice to manage the lifecycle. However, those details are kept completely abstract from the author of the
 * particular Element as it is possible to use any library inside of the Element itself.
 *
 * Supported features of Elements:
 * <ul>
 *     <li>Unlike Java Modules, they may span multiple Jar files or sources so long as there is a single {@link ClassLoader} which aggregates all code</li>
 *     <li>Parent-child relationships, where children may inherit from the parent and parents may request resources from children.</li>
 *     <li>Weak "honor system" encapsulation. There are no restrictions on reflection, so no need to worry about IoC containers needing special consideration.</li>
 *     <li>Isolation of dependencies of the core Elements product. You can use whatever third party libraries you wish and it will not break the system.</li>
 *     <li>Greater flexibility in structure. Existing applications do not need to reorganize as Modules if they weren't build that way originally.</li>
 * </ul>
 */
public interface Element extends AutoCloseable {

    /**
     * Gets the {@link ElementDefinitionRecord} which provides
     *
     * @return the name of the element.
     */
    ElementRecord getElementRecord();

    /**
     * Gets the {@link ServiceLocator} associated with this element.
     *
     * @return the {@link ServiceLocator}
     */
    ServiceLocator getServiceLocator();

    /**
     * Gets the {@link ElementRegistry} which manages this {@link Element}.
     *
     * @return the {@link ElementRegistry}
     */
    ElementRegistry getElementRegistry();

    /**
     * Begins building the {@link ElementScope} for this thread.
     *
     * @return the {@link ElementScope.Builder} to begin building the scope.
     */
    ElementScope.Builder withScope();

    /**
     * Gets the current {@link ElementScope}, if available. Throwing an exception
     * if there is no scope.
     *
     * @return the {@link ElementScope}, never null.
     * @throws IllegalStateException if no scope is current
     */
    default ElementScope getCurrentScope() throws IllegalStateException {
        return findCurrentScope().orElseThrow(IllegalStateException::new);
    }

    /**
     * Attempts to find the current scope, returning an {@link Optional} containing the scope, if present.
     *
     * @return an {@link Optional} with the {@link ElementScope}
     */
    Optional<ElementScope> findCurrentScope();

    /**
     * Publishes an {@link Event} to the {@link Element}. All public {@link Element} instances will receive the
     * event provided that the arguments match the method, or that the method {}
     * @param event
     */
    void publish(Event event);

    /**
     * Registers a callback to be executed when this {@link Element} is closed. The callback will be invoked
     * before the element's resources are released. Multiple callbacks can be registered and will be executed
     * in registration order.
     *
     * @param onClose the callback to execute on close
     * @return
     */
    Subscription onClose(Consumer<Element> onClose);

    /**
     * Closes the element and releases any resources associated with the element. Future calls to this instance will be
     * undefined and may result in exceptions. The specific behavior is up to the element implementation.
     */
    void close();

}
