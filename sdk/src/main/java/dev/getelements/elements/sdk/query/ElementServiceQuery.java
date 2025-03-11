package dev.getelements.elements.sdk.query;

import dev.getelements.elements.sdk.Callback;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.record.ElementServiceKey;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The {@link Query} for the service from within an {@link Element}.
 *
 * @param element
 * @param serviceKey
 * @param <ServiceT>
 */
public record ElementServiceQuery<ServiceT>(
        Element element,
        ElementServiceKey<? extends ServiceT> serviceKey) implements Query<ServiceT> {

    @Override
    public Optional<ServiceT> find() throws QueryException {
        return element.getServiceLocator().findInstance(serviceKey()).map(Supplier::get);
    }

    /**
     * Queries fo ra {@link Callback} with the supplied method name and parameter types.
     */
    public ElementCallbackQuery<? extends ServiceT> callback(final String methodName,
                                                             final Class<?> ... parameters) {
        return findCallback(methodName, parameters).orElseThrow(QueryException::new);
    }

    /**
     * Queries for a {@link Callback} with the supplied method name and parameter types.
     *
     * @param methodName the method name
     * @param parameters the method parameters
     * @return an {@link ElementCallbackQuery}
     */
    public Optional<ElementCallbackQuery<? extends ServiceT>> findCallback(
            final String methodName,
            final Class<?> ... parameters) {
        return element
                .getServiceLocator()
                .findInstance(serviceKey())
                .map(supplier -> new ElementCallbackQuery<>(
                    serviceKey(),
                    supplier,
                    methodName,
                    List.of(parameters))
                );

    }

}
