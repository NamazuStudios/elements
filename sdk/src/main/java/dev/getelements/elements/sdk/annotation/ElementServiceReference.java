package dev.getelements.elements.sdk.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation that specifies a reference to a service by its type and optionally its name. This is primarily
 * used as a parameter to other annotations (such as {@link ElementEventConsumer#via()}) to specify which
 * service should be used for routing or lookup operations.
 *
 * <p>A service reference consists of:</p>
 * <ul>
 *     <li><strong>Service Type</strong> ({@link #value()}): The class or interface of the service to look up.
 *         This is typically a public service interface annotated with {@link ElementPublic}.</li>
 *     <li><strong>Service Name</strong> ({@link #name()}): An optional qualifier when multiple services of
 *         the same type exist. If empty (the default), an unnamed service is assumed.</li>
 * </ul>
 *
 * <h3>Usage with Event Consumers</h3>
 * The most common use case is routing events through a service interface:
 * <pre>{@code
 * @ElementServiceExport
 * public class MyServiceImpl implements MyService {
 *     @ElementEventConsumer(
 *         value = "my.event",
 *         via = @ElementServiceReference(MyService.class)
 *     )
 *     public void onEvent(String arg) {
 *         // Event routed through MyService lookup
 *     }
 * }
 * }</pre>
 *
 * <h3>Named Services</h3>
 * When multiple services of the same type exist with different names:
 * <pre>{@code
 * @ElementServiceExport
 * @Named("primary")
 * public class PrimaryServiceImpl implements MyService { }
 *
 * // Reference the named service
 * @ElementServiceReference(value = MyService.class, name = "primary")
 * }</pre>
 *
 * @see ElementEventConsumer
 * @see ElementServiceExport
 * @see ElementPublic
 * @since 3.7
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementServiceReference {

    /**
     * The service type to reference. This is the class or interface that will be used to look up
     * the service instance via the {@link dev.getelements.elements.sdk.ServiceLocator}.
     *
     * <p>Typically this is a public service interface annotated with {@link ElementPublic}, but it
     * can also be a concrete implementation class if that class is directly exported via
     * {@link ElementServiceExport}.</p>
     *
     * @return the service type to look up
     */
    Class<?> value();

    /**
     * The optional service name qualifier. When specified, the service lookup will match both the
     * service type and this name. This is necessary when multiple services of the same type exist
     * and are distinguished by name (typically using {@link jakarta.inject.Named}).
     *
     * <p>If empty (the default), the service lookup will find an unnamed service, or will fail if
     * only named services of the specified type exist.</p>
     *
     * <h4>Example:</h4>
     * <pre>{@code
     * // Service with name
     * @ElementServiceExport
     * @Named("primary")
     * public class PrimaryImpl implements MyService { }
     *
     * // Reference it by name
     * @ElementServiceReference(value = MyService.class, name = "primary")
     * }</pre>
     *
     * @return the service name, or empty string for unnamed services
     * @see jakarta.inject.Named
     */
    String name() default  "";

}
