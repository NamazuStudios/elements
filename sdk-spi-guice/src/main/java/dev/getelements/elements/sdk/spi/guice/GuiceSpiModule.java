package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.spi.Elements;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation.DefaultImplementation;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.spi.guice.record.GuiceElementModuleRecord;
import dev.getelements.elements.sdk.spi.guice.record.GuiceOptionsRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementServiceRecord;
import jakarta.inject.Provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.spi.guice.annotations.GuiceOptions.LoadingStrategy.GUICE_MODULE_ONLY;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * Used to bind services.
 */
public class GuiceSpiModule extends PrivateModule {

    private final ElementRegistry parent;

    private final ElementRecord elementRecord;

    private final GuiceOptionsRecord options;

    private final List<Module> guiceElementModules;

    private final Map<Key<?>, Provider<Object>> parentFallbackProviders;

    public GuiceSpiModule(
            final ElementRegistry parent,
            final ElementRecord elementRecord) {

        this.parent = requireNonNull(parent, "parent");
        this.elementRecord = requireNonNull(elementRecord, "elementRecord");

        final var elementPackage = elementRecord.definition().pkg();
        this.options = GuiceOptionsRecord.fromPackage(elementPackage);

        this.guiceElementModules = GuiceElementModuleRecord
                .fromPackage(elementPackage)
                .map(GuiceElementModuleRecord::newModule)
                .toList();

        // Validated here, eagerly, rather than inside configure() -- Guice catches exceptions thrown from a
        // module's configure() and folds them into its own CreationException alongside whatever other errors
        // it collects (e.g. the very [Guice/ExposedButNotBound] error this is meant to preempt), so a throw from
        // configure() never actually reaches the caller as a clean SdkException. Throwing here, before
        // Guice.createInjector() is ever invoked, does.
        if (options.strict()) {
            validateDeferredKeysAreBound(elementPackage);
        }

        // Same rationale: deferred keys that no installed @GuiceElementModule (nor this module's own
        // @ElementServiceImplementation) binds are resolved against the parent element registry here, eagerly,
        // so a missing provider surfaces as a clean SdkException rather than Guice's [Guice/ExposedButNotBound]
        // CreationException (issue #32). GUICE_MODULE_ONLY opts out entirely -- it defers to the author's own
        // modules per GuiceOptions' documented contract.
        this.parentFallbackProviders = options.strategy() != GUICE_MODULE_ONLY && !options.strict()
                ? resolveParentFallbackProviders()
                : Map.of();

    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {

        final var attributes = elementRecord.attributes().asProperties();

        binder().requireExplicitBindings();
        bindProperties(binder(), attributes);

        final var targets = new HashSet<Class<?>>();
        final var ownKeys = new HashSet<Key<?>>();

        if (options.strategy() == GUICE_MODULE_ONLY) {

            // Defers exclusively to the installed @GuiceElementModule(s); ElementService#implementation() is
            // ignored entirely (per GuiceOptions' documented contract) to avoid double-defining bindings the
            // author's own module already supplies.
            elementRecord.services().forEach(esr -> exposeService(ownKeys, esr));

        } else {

            elementRecord
                    .services()
                    .stream()
                    .filter(esr -> DefaultImplementation.class.equals(esr.implementation().type()))
                    .forEach(esr -> exposeService(ownKeys, esr));

            elementRecord
                    .services()
                    .stream()
                    .filter(esr -> !DefaultImplementation.class.equals(esr.implementation().type()))
                    .forEach(esr -> bindAndExposeService(targets, ownKeys, esr));

        }

        elementRecord
                .dependencies()
                .stream()
                .flatMap(dep -> dep.findDependencies(parent))
                .forEach(element -> bindDependentElement(ownKeys, element));

        guiceElementModules.forEach(this::install);

        parentFallbackProviders.forEach((key, provider) -> bind((Key<Object>) key).toProvider(provider));

    }

    /**
     * Resolves deferred keys (see {@link #computeDeferredKeys()}) that neither an installed
     * {@code @GuiceElementModule} nor this module's own {@code @ElementServiceImplementation} binds: the key is
     * delegated to the ServiceLocator of a parent element that exports it -- mirroring {@link #bindDependentService}
     * -- so an unbound expose-only service no longer surfaces as Guice's generic {@code [Guice/ExposedButNotBound]}
     * error (issue #32). If no parent element provides it either, a clear {@link SdkException} names the exact
     * unresolvable service.
     *
     * @return providers for each deferred key that only a parent element provides
     */
    private Map<Key<?>, Provider<Object>> resolveParentFallbackProviders() {

        final var deferredKeys = computeDeferredKeys();

        if (deferredKeys.isEmpty()) {
            return Map.of();
        }

        final var moduleBoundKeys = Elements
                .getElements(guiceElementModules)
                .stream()
                .filter(element -> element instanceof Binding<?>)
                .map(element -> ((Binding<?>) element).getKey())
                .collect(toSet());

        final var ownBoundKeys = elementRecord
                .services()
                .stream()
                .filter(esr -> !DefaultImplementation.class.equals(esr.implementation().type()))
                .flatMap(esr -> {
                    final Stream<Key<?>> exportedKeys = exportedKeys(esr).stream();
                    return Stream.concat(Stream.<Key<?>>of(Key.get(esr.implementation().type())), exportedKeys);
                })
                .collect(toSet());

        final var providers = new HashMap<Key<?>, Provider<Object>>();

        deferredKeys
                .stream()
                .filter(key -> !moduleBoundKeys.contains(key))
                .filter(key -> !ownBoundKeys.contains(key))
                .forEach(key -> providers.put(key, findParentProvider(key)));

        return providers;

    }

    @SuppressWarnings("unchecked")
    private Provider<Object> findParentProvider(final Key<?> key) {

        for (final Element element : parent.stream().toList()) {

            final var export = element
                    .getElementRecord()
                    .services()
                    .stream()
                    .filter(esr -> esr.export().expose())
                    .filter(esr -> exportedKeys(esr).contains(key))
                    .map(ElementServiceRecord::export)
                    .findFirst();

            if (export.isEmpty()) continue;

            final var locator = element.getServiceLocator();
            final var type = (Class<Object>) key.getTypeLiteral().getRawType();

            if (export.orElseThrow().isNamed()) {
                return () -> locator.getInstance(type, export.orElseThrow().name());
            } else {
                return () -> locator.getInstance(type);
            }

        }

        throw new SdkException(
                "Element package '" + elementRecord.definition().pkg().getName() + "' exports " + key +
                " with no @ElementServiceImplementation, no installed @GuiceElementModule binds it, and no " +
                "Element in the parent registry provides it. Provide an @ElementServiceImplementation, add a " +
                "@GuiceElementModule that binds it, or depend on an Element that provides it."
        );

    }

    /**
     * Computes the set of exported keys that this module will {@code expose()} without binding itself -- i.e. the
     * ones relying on something else (an installed {@code @GuiceElementModule}) to have bound them.
     */
    private Set<Key<?>> computeDeferredKeys() {

        final var deferredKeys = new HashSet<Key<?>>();

        if (options.strategy() == GUICE_MODULE_ONLY) {
            elementRecord.services().forEach(esr -> deferredKeys.addAll(exportedKeys(esr)));
        } else {
            elementRecord
                    .services()
                    .stream()
                    .filter(esr -> DefaultImplementation.class.equals(esr.implementation().type()))
                    .forEach(esr -> deferredKeys.addAll(exportedKeys(esr)));
        }

        return deferredKeys;

    }

    /**
     * With {@link GuiceOptionsRecord#strict()} enabled, verifies every deferred key (see {@link #computeDeferredKeys()})
     * is actually bound by one of the installed {@code @GuiceElementModule}s, raising a clear, actionable error at
     * load time instead of letting Guice's generic {@code [Guice/ExposedButNotBound]} error surface later at
     * injector-creation time.
     */
    private void validateDeferredKeysAreBound(final Package elementPackage) {

        final var deferredKeys = computeDeferredKeys();

        if (deferredKeys.isEmpty()) {
            return;
        }

        final var boundKeys = Elements
                .getElements(guiceElementModules)
                .stream()
                .filter(element -> element instanceof Binding<?>)
                .map(element -> ((Binding<?>) element).getKey())
                .collect(toSet());

        final var unbound = deferredKeys
                .stream()
                .filter(key -> !boundKeys.contains(key))
                .toList();

        if (!unbound.isEmpty()) {
            throw new SdkException(
                    "Element package '" + elementPackage.getName() + "' declares GuiceOptions(strategy=" +
                    options.strategy() + ", strict=true), but the following exported service(s) are not bound " +
                    "by any installed @GuiceElementModule: " + unbound + ". Provide an " +
                    "@ElementServiceImplementation, or add a @GuiceElementModule that explicitly binds them."
            );
        }

    }

    private Set<Key<?>> exportedKeys(final ElementServiceRecord elementServiceRecord) {

        final var export = elementServiceRecord.export();

        final var keys = export.isNamed()
                ? export.exposed().stream().map(anInterface -> Key.get(anInterface, named(export.name())))
                : export.exposed().stream().map(Key::get);

        return keys.collect(toSet());

    }

    private void exposeService(final Set<Key<?>> ownKeys,
                               final ElementServiceRecord elementServiceRecord) {

        final var export = elementServiceRecord.export();

        final var keys = export.isNamed()
                ? export.exposed().stream().map(anInterface -> Key.get(anInterface, named(export.name())))
                : export.exposed().stream().map(Key::get);

        keys.forEach(k -> {
            ownKeys.add(k);
            expose(k);
        });

    }

    private void bindAndExposeService(final Set<Class<?>> targets,
                                      final Set<Key<?>> ownKeys,
                                      final ElementServiceRecord elementServiceRecord) {

        final var export = elementServiceRecord.export();
        final var implementation = elementServiceRecord.implementation();

        if (targets.add(implementation.type())) {

            bind(implementation.type());

            if (implementation.expose()) {
                expose(implementation.type());
            }

        }

        final var keys = export.isNamed()
                ? export.exposed().stream().map(anInterface -> Key.get(anInterface, named(export.name())))
                : export.exposed().stream().map(Key::get);

        keys.forEach(k -> {
            ownKeys.add(k);
            bind(k).to((Class)implementation.type());
            expose(k);
        });

    }

    private void bindDependentElement(final Set<Key<?>> ownKeys, final Element element) {
        element.getElementRecord()
                .services()
                .forEach(esr -> bindDependentService(ownKeys, element, esr));
    }

    private void bindDependentService(
            final Set<Key<?>> ownKeys,
            final Element element,
            final ElementServiceRecord elementServiceRecord) {

        final var export = elementServiceRecord.export();

        if (!export.expose()) return;

        export.exposed()
                .stream()
                .forEach(aClass -> {

                    final Key<Object> key = export.isNamed()
                            ? (Key<Object>) Key.get(aClass, named(export.name()))
                            : (Key<Object>) Key.get(aClass);

                    if (ownKeys.contains(key)) return;

                    final Provider<Object> provider = () -> export.isNamed()
                            ? element.getServiceLocator().getInstance(aClass, export.name())
                            : element.getServiceLocator().getInstance(aClass);

                    bind(key).toProvider(provider);
                    expose(key);

                });
    }

}
