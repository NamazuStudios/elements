package dev.getelements.elements.sdk.query;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.List.copyOf;

public record ElementMethodQuery<ServiceT>(
        ElementServiceQuery<ServiceT> service,
        String name,
        List<Class<?>> parameters) implements Query<Method> {

    public ElementMethodQuery {
        if (parameters == null) throw new QueryException();
        parameters = copyOf(parameters);
    }

    @Override
    public Optional<Method> find() throws QueryException {
        return Stream.of(service().serviceKey().type().getDeclaredMethods())
                .filter(method -> name().equals(method.getName()))
                .filter(this::checkParameters)
                .findFirst();
    }

    private boolean checkParameters(final Method method) {

        final var parameters = method.getParameterTypes();

        if (parameters.length != parameters().size()) {
            return false;
        }

        return parameters().equals(List.of(parameters));

    }

}
