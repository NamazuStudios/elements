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
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.dao.rt.guice.RTGitApplicationModule;
import com.namazustudios.socialengine.dao.rt.guice.RTDaoModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.service.guice.JacksonHttpClientModule;
import com.namazustudios.socialengine.service.firebase.guice.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.service.guice.OctetStreamJsonMessageBodyReader;
import com.namazustudios.socialengine.service.notification.guice.GuiceStandardNotificationFactoryModule;
import com.namazustudios.socialengine.service.notification.guice.NotificationServiceModule;
import com.namazustudios.socialengine.util.AppleDateFormat;
import org.apache.bval.guice.ValidationModule;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.APPLE_ITUNES;

/**
 * Created by patricktwohig on 3/19/15.
 */
@WebListener
public class GuiceMain extends GuiceServletContextListener {

    private Injector injector;

    private ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        servletContext = servletContextEvent.getServletContext();
        super.contextInitialized(servletContextEvent);
        servletContext.setAttribute(GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME, injector);

        final ConnectionMultiplexer connectionMultiplexer = injector.getInstance(ConnectionMultiplexer.class);
        connectionMultiplexer.start();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        super.contextDestroyed(servletContextEvent);

        final ConnectionMultiplexer connectionMultiplexer = injector.getInstance(ConnectionMultiplexer.class);
        connectionMultiplexer.stop();

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
            new ServicesModule(),
            new NotificationServiceModule(),
            new GuiceStandardNotificationFactoryModule(),
            new FirebaseAppFactoryModule(),
            new RedissonServicesModule(),
            new SecurityModule(),
            new MongoCoreModule(),
            new MongoDaoModule(),
            new MongoSearchModule(),
            new RTFilesystemGitLoaderModule(),
            new RTDaoModule(),
            new RTGitApplicationModule(),
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
