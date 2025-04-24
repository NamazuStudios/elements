package dev.getelements.elements.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.service.guice.firebase.FirebaseAppFactoryModule;
import dev.getelements.elements.service.security.ElementScopeOptionalProfileProvider;
import dev.getelements.elements.service.security.ElementScopeProfileSupplierProvider;
import dev.getelements.elements.service.security.ElementScopeUserProvider;
import jakarta.inject.Provider;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Configures all of the services, using a {@link Scope} for {@link User}, {@link Profile} injections.
 */
public class ServicesModule extends AbstractModule {

    private final Scope scope;

    /**
     * Configures all services to use the following {@link Scope} and {@link Attributes} {@link Provider<Attributes>}
     * type, which is used to match the {@link Attributes} to the associated {@link Scope}.
     *
     * @param scope the scope
     **/
    public ServicesModule(final Scope scope) {
        this.scope = scope;
    }

    @Override
    protected void configure() {

        final var optionalProfileKey = Key.get(new TypeLiteral<Optional<Profile>>() {});

        bind(User.class).toProvider(ElementScopeUserProvider.class);

        bind(new TypeLiteral<Optional<Profile>>() {}).toProvider(ElementScopeOptionalProfileProvider.class);
        bind(new TypeLiteral<Supplier<Profile>>() {}).toProvider(ElementScopeProfileSupplierProvider.class);

        install(new ServiceUtilityModule());
        install(new DatabaseHealthStatusDaoAggregator());

        install(new StandardServicesModule());
        install(new UnscopedServicesModule());
        install(new ScopedServicesModule(scope));

        install(new FlowBlockchainSupportModule());
        install(new Web3jBlockchainSupportModule());
        install(new OmniBlockchainServicesUtilityModule());
        install(new NearBlockchainSupportModule());
        install(new FirebaseAppFactoryModule());
        install(new FirebaseNotificationFactoryModule());

        install(new EvmInvokerModule());
        install(new FlowInvokerModule());
        install(new NearInvokerModule());

    }

}
