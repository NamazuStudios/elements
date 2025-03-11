package dev.getelements.elements.sdk.query;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.exception.SdkServiceNotFoundException;
import dev.getelements.elements.sdk.record.ElementServiceKey;

import java.util.Optional;

/**
 * The beginning queyr for a {@link Element} instance based on a {@link ElementRegistry}.
 *
 * @param registry
 * @param name
 * @param index
 */
public record ElementQuery(ElementRegistry registry, String name, int index) implements Query<Element> {

    /**
     * Gets the {@link Element} for this {@link ElementQuery}.
     *
     * @return the {@link Element}
     * @throws QueryException
     */
    @Override
    public Optional<Element> find() throws QueryException {
        return registry().find(name()).skip(index()).findFirst();
    }

    /**
     * Finds a {@link ElementServiceQuery} from the selected {@link Element}.
     *
     * @param serviceKeyString the service key string
     * @return the {@link ElementServiceQuery}
     */
    public Optional<ElementServiceQuery<?>> findService(final String serviceKeyString) {
        return find().flatMap(element -> element
                .getElementRecord()
                .tryParseServiceKey(serviceKeyString)
                .flatMap(this::findService)
        );
    }

    /**
     * Finds a {@link ElementServiceQuery} from the selected {@link Element}.
     *
     * @param serviceKey the service key string
     * @return the {@link ElementServiceQuery}
     */
    public <ServiceT> Optional<ElementServiceQuery<ServiceT>> findService(final ElementServiceKey<ServiceT> serviceKey) {
        return find().flatMap(element -> element
                        .getServiceLocator()
                        .findInstance(serviceKey)
                        .map(_ignored -> new ElementServiceQuery<>(element, serviceKey))
                );
    }

    /**
     * Gets a {@link ElementServiceQuery} from the selected {@link Element}.
     *
     * @param serviceKeyString the service key string
     * @return the {@link ElementServiceQuery}
     */
    public ElementServiceQuery<?> service(final String serviceKeyString) {
        return findService(serviceKeyString).orElseThrow(() -> new SdkServiceNotFoundException(serviceKeyString));
    }

    /**
     * Gets the {@link ElementServiceQuery} for the specific {@link ElementServiceKey}.
     *
     * @param serviceKey the {@link ElementServiceKey}
     *
     * @return the {@link ElementServiceQuery}
     * @param <ServiceT>
     */
    public <ServiceT> ElementServiceQuery<ServiceT> service(final ElementServiceKey<ServiceT> serviceKey) {
        return findService(serviceKey).orElseThrow(() -> new SdkServiceNotFoundException(serviceKey.toString()));
    }

}
