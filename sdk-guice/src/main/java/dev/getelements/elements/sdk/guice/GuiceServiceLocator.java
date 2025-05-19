package dev.getelements.elements.sdk.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.multibindings.MapKey;
import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.record.ElementServiceKey;
import dev.getelements.elements.sdk.util.ReentrantThreadLocal;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;

/**
 * Created by patricktwohig on 9/1/15.
 */
public class GuiceServiceLocator implements ServiceLocator {

    private Injector injector;

    private ReentrantThreadLocal<ElementScope> scopeThreadLocal;

    @Override
    public <T> Optional<Supplier<T>> findInstance(final ElementServiceKey<T> elementServiceKey) {

        final var guiceKey = elementServiceKey.isNamed()
            ? Key.get(elementServiceKey.type(), named(elementServiceKey.name()))
            : Key.get(elementServiceKey.type());

        final var binding = getInjector().getBinding(guiceKey);
        return Optional.ofNullable(binding).map(b -> b.getProvider()::get);

    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public ReentrantThreadLocal<ElementScope> getScopeThreadLocal() {
        return scopeThreadLocal;
    }

    @Inject
    public void setScopeThreadLocal(ReentrantThreadLocal<ElementScope> scopeThreadLocal) {
        this.scopeThreadLocal = scopeThreadLocal;
    }

}
