package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rt.git.FilesystemGitLoaderModule;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.guice.ClusterContextFactoryModule;
import com.namazustudios.socialengine.service.guice.*;
import com.namazustudios.socialengine.service.guice.firebase.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.util.AppleDateFormat;
import ru.vyarus.guice.validator.ValidationModule;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.text.DateFormat;
import java.util.Properties;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.APPLE_ITUNES;

/**
 * Created by patricktwohig on 3/19/15.
 */
@WebListener
@Deprecated(forRemoval = true)
public class GuiceMain extends GuiceServletContextListener {

    private Injector injector;

    private ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        servletContext = servletContextEvent.getServletContext();
        super.contextInitialized(servletContextEvent);
        servletContext.setAttribute(GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME, injector);

        final InstanceConnectionService connectionService = injector.getInstance(InstanceConnectionService.class);
        connectionService.start();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        super.contextDestroyed(servletContextEvent);

        final InstanceConnectionService connectionService = injector.getInstance(InstanceConnectionService.class);
        connectionService.stop();

        servletContext.removeAttribute(GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME);
        servletContext = null;
        injector = null;

    }

    @Override
    protected Injector getInjector() {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier(servletContext.getClassLoader());

        final Properties properties = defaultConfigurationSupplier.get();
        final String apiRoot = properties.getProperty(Constants.API_PREFIX);

        final FacebookBuiltinPermissionsSupplier facebookBuiltinPermissionsSupplier;
        facebookBuiltinPermissionsSupplier = new FacebookBuiltinPermissionsSupplier(servletContext.getClassLoader());

        return injector = Guice.createInjector(
            new ConfigurationModule(defaultConfigurationSupplier),
            new FacebookBuiltinPermissionsModule(facebookBuiltinPermissionsSupplier),
            new JerseyModule(apiRoot) {
                @Override
                protected void configureResoures() {
                    enableAllResources();
                }
            },
            new RestAPIServicesModule(),
            new NotificationServiceModule(),
            new GuiceStandardNotificationFactoryModule(),
            new FirebaseAppFactoryModule(),
            new RestAPIRedissonServicesModule(),
            new RestAPISecurityModule(),
            new MongoCoreModule(),
            new MongoDaoModule(),
            new MongoSearchModule(),
            new FilesystemGitLoaderModule(),
            new ClusterContextFactoryModule(),
            new ValidationModule(),
            new GameOnInvokerModule(),
            new AppleIapReceiptInvokerModule(),
            new JacksonHttpClientModule()
            .withRegisteredComponent(OctetStreamJsonMessageBodyReader.class)
            .withDefaultObjectMapperProvider(() -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            }).withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
                final ObjectMapper objectMapper = new ObjectMapper();
                final DateFormat dateFormat = new AppleDateFormat();
                objectMapper.setDateFormat(dateFormat);
                objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
                return objectMapper;
            })
        );

    }

}
