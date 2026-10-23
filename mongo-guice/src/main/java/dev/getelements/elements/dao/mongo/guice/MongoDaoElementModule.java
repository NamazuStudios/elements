package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.TypeLiteral;
import dev.getelements.elements.dao.mongo.provider.MongoAtomicReferenceDataStoreProvider;
import dev.getelements.elements.dao.mongo.provider.MorphiaConfigProvider;
import dev.getelements.elements.sdk.dao.ElementEntityRegistrar;
import dev.getelements.elements.sdk.dao.JwtSigningKeyDao;
import dev.getelements.elements.sdk.dao.MachineTokenRevocationDao;
import dev.getelements.elements.sdk.dao.OidcProviderDao;
import dev.getelements.elements.sdk.guice.SharedElementModule;
import dev.morphia.Datastore;
import dev.morphia.config.MorphiaConfig;

import java.util.concurrent.atomic.AtomicReference;

public class MongoDaoElementModule extends SharedElementModule {

    public MongoDaoElementModule() {
        super("dev.getelements.elements.sdk.dao");
    }

    @Override
    protected void configureElement() {
        // Bind these directly here (not in nested MongoDaoModule) so that expose() below
        // sees a direct binding — Guice's PrivateModule.expose() does not accept forwarding
        // bindings propagated up from child private modules.
        bind(MorphiaConfig.class)
                .toProvider(MorphiaConfigProvider.class);
        bind(new TypeLiteral<AtomicReference<Datastore>>(){})
                .toProvider(MongoAtomicReferenceDataStoreProvider.class)
                .asEagerSingleton();
        install(new PreDatastoreMigrationModule());
        install(new MongoDaoModule());
        install(new MongoGridFSLargeObjectBucketModule());
        expose(ElementEntityRegistrar.class);
        // The signing-key DAO is intentionally not @ElementServiceExport-annotated: it is internal
        // server infrastructure and must not be visible to Elements. Bind and expose it here
        // directly, since Guice's PrivateModule.expose() does not accept forwarding bindings
        // propagated up from the nested MongoDaoModule.
        expose(JwtSigningKeyDao.class);
        // The OIDC provider DAO is likewise internal server infrastructure (client registrations,
        // authorization codes, grants, refresh tokens) and must not be visible to Elements. The nested
        // MongoDaoModule binds it; publishing it here makes it resolvable by the service-side modules.
        expose(OidcProviderDao.class);
        // The machine-token revocation denylist is likewise internal server infrastructure. Publishing it
        // here makes it resolvable by the service-side modules that back MachineTokenService.
        expose(MachineTokenRevocationDao.class);
        // Expose to the outer scope so MongoSdkModule.DatastoreFromRef can inject it.
        expose(new TypeLiteral<AtomicReference<Datastore>>(){});
    }

}
