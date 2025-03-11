package dev.getelements.elements.sdk.query;

import dev.getelements.elements.sdk.Callback;
import dev.getelements.elements.sdk.record.ElementServiceKey;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.List.copyOf;
import static java.util.Objects.requireNonNull;

/**
 * Finds a {@link Callback} from within a service type.
 */
public record ElementCallbackQuery<ServiceT>(
        ElementServiceKey<? extends ServiceT> serviceKey,
        Supplier<? extends ServiceT> supplier,
        String methodName,
        List<Class<?>> parameters) implements Query<Callback<Object>> {

    public ElementCallbackQuery {
        parameters = copyOf(parameters);
        parameters = requireNonNull(parameters, "parameters");
        serviceKey = requireNonNull(serviceKey, "serviceKey");
        methodName = requireNonNull(methodName, "methodName");
    }

    @Override
    public Optional<Callback<Object>> find() throws QueryException {
        return Stream.of(serviceKey.type().getDeclaredMethods())
                .filter(method -> methodName().equals(method.getName()))
                .filter(this::checkParameters)
                .map(method -> Modifier.isStatic(method.getModifiers())
                        ? new MethodCallbackRecord<>(null, method)
                        : new MethodCallbackRecord<>(supplier().get(), method))
                .map(c -> (Callback<Object>)c)
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
