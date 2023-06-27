//package dev.getelements.elements.rest.guice;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.inject.AbstractModule;
//import dev.getelements.elements.Constants;
//import dev.getelements.elements.annotation.FacebookPermission;
//import dev.getelements.elements.config.FacebookBuiltinPermissionsSupplier;
//import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
//import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
//import dev.getelements.elements.dao.mongo.guice.MongoSearchModule;
//import dev.getelements.elements.guice.*;
//import dev.getelements.elements.rt.fst.FSTPayloadReaderWriterModule;
//import dev.getelements.elements.rt.id.InstanceId;
//import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
//import dev.getelements.elements.rt.remote.guice.ClusterContextFactoryModule;
//import dev.getelements.elements.rt.remote.guice.InstanceDiscoveryServiceModule;
//import dev.getelements.elements.rt.remote.guice.SimpleInstanceModule;
//import dev.getelements.elements.rt.remote.guice.SimpleRemoteInvokerRegistryModule;
//import dev.getelements.elements.rt.remote.jeromq.guice.*;
//import dev.getelements.elements.service.guice.AppleIapReceiptInvokerModule;
//import dev.getelements.elements.service.guice.GuiceStandardNotificationFactoryModule;
//import dev.getelements.elements.service.guice.NotificationServiceModule;
//import dev.getelements.elements.service.guice.firebase.FirebaseAppFactoryModule;
//import ru.vyarus.guice.validator.ValidationModule;
//
//import java.util.List;
//import java.util.Properties;
//import java.util.function.Supplier;
//
//import static dev.getelements.elements.rt.id.InstanceId.randomInstanceId;
//
//public class EmbeddedRestAPIModule extends AbstractModule {
//
//    private final Supplier<Properties> configurationSupplier;
//
//    private final Supplier<List<FacebookPermission>> facebookPermissionSupplier;
//
//    public EmbeddedRestAPIModule(final Supplier<Properties> propertiesSupplier) {
//        this(propertiesSupplier, new FacebookBuiltinPermissionsSupplier());
//    }
//
//    public EmbeddedRestAPIModule(final Supplier<Properties> configurationSupplier,
//                                 final Supplier<List<FacebookPermission>> facebookPermissionSupplier) {
//        this.configurationSupplier = configurationSupplier;
//        this.facebookPermissionSupplier = facebookPermissionSupplier;
//    }
//
//    @Override
//    protected void configure() {
//
//        final Properties properties = configurationSupplier.get();
//        final String apiRoot = properties.getProperty(Constants.API_PREFIX);
//
//        bind(ObjectMapper.class).asEagerSingleton();
//
//        install(new InstanceDiscoveryServiceModule(configurationSupplier));
//        install(new ConfigurationModule(() -> properties));
//        install(new FacebookBuiltinPermissionsModule(facebookPermissionSupplier));
//        install(new RestAPIJerseyModule(apiRoot));
//        install(new StandardServletServicesModule());
//        install(new NotificationServiceModule());
//        install(new GuiceStandardNotificationFactoryModule());
//        install(new FirebaseAppFactoryModule());
//        install(new StandardServletRedissonServicesModule());
//        install(new StandardServletSecurityModule());
//        install(new MongoCoreModule());
//        install(new MongoDaoModule());
//        install(new MongoSearchModule());
//        install(new ZContextModule());
//        install(new ClusterContextFactoryModule());
//        install(new ValidationModule());
//        install(new AppleIapReceiptInvokerModule());
//        install(new JeroMQAsyncConnectionServiceModule());
//        install(new JeroMQInstanceConnectionServiceModule());
//        install(new JeroMQRemoteInvokerModule());
//        install(new JeroMQControlClientModule());
//        install(new SimpleRemoteInvokerRegistryModule());
//        install(new SimpleInstanceModule());
//        install(new FSTPayloadReaderWriterModule());
//        install(new JerseyHttpClientModule());
//        bind(InstanceId.class).toInstance(randomInstanceId());
//
//    }
//}
