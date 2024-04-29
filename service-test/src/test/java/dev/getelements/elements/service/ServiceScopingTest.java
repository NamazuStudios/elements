package dev.getelements.elements.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import dev.getelements.elements.rt.annotation.DefaultBindingAnnotationFactory;
import dev.getelements.elements.rt.annotation.Expose;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

public class ServiceScopingTest {

    private static final Logger logger = LoggerFactory.getLogger(ServiceScopingTest.class);

    private Injector injector;

    @BeforeClass
    public void setup() {
        injector = Guice.createInjector(new MockServiceTestModule());
    }

    @DataProvider
    public Object[][] scopedServiceClasses() {
        final var classLaoder = ClassLoader.getSystemClassLoader();
        final var reflections = new Reflections("dev.getelements.elements.service", classLaoder);
        final var exposedServices = reflections.getTypesAnnotatedWith(Expose.class);
        return exposedServices
                .stream()
                .filter(Class::isInterface)
                .map(c -> new Object[]{c}).toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] unscopedServiceClasses() {

        final var exclude = Set.of(
                FriendService.class,
                RankService.class,
                FacebookFriendService.class,
                ScoreService.class,
                PSNApplicationConfigurationService.class
        );

        final var classLaoder = ClassLoader.getSystemClassLoader();
        final var reflections = new Reflections("dev.getelements.elements.service", classLaoder);
        final var exposedServices = reflections.getTypesAnnotatedWith(Expose.class);
        return exposedServices
                .stream()
                .filter(c -> !exclude.contains(c))
                .filter(Class::isInterface)
                .map(c -> new Object[]{c}).toArray(Object[][]::new);

    }

    @Test(dataProvider = "scopedServiceClasses")
    public void testScopedServices(final Class<?> serviceClass) {
        try {
            MockServiceTestModule.enter();
            logger.error("Testing {}", serviceClass);
            injector.getInstance(serviceClass);
        } finally {
            MockServiceTestModule.exit();
        }
    }

    @Test(dataProvider = "unscopedServiceClasses")
    public void testUnscopedServices(final Class<?> serviceClass) {
        logger.error("Testing {}", serviceClass);
        final var key = Key.get(serviceClass, Unscoped.class);
        injector.getInstance(key);
    }


    @Test(dataProvider = "unscopedServiceClasses")
    public void testUnscopedServicesWithBindingFactory(final Class<?> serviceClass) {
        logger.error("Testing {}", serviceClass);
        final var factory = new DefaultBindingAnnotationFactory();
        final var annotation = factory.construct(serviceClass, Unscoped.class);
        final var key = Key.get(serviceClass, annotation);
        injector.getInstance(key);
    }

}
