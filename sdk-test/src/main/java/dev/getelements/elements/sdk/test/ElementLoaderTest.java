package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.test.element.TestService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_A;
import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_B;
import static dev.getelements.elements.sdk.test.element.TestService.TEST_ELEMENT_EVENT_1;
import static dev.getelements.elements.sdk.test.element.TestService.TEST_ELEMENT_EVENT_2;

public class ElementLoaderTest {

    private final URL elementUrl;

    private final String elementPackage;

    private final ElementRegistry elementRegistry;

    private final ClassLoader  baseClassLoader = ClassLoader.getSystemClassLoader();

    private static final TestArtifactRegistry testArtifactRegistry = new TestArtifactRegistry();

    @Factory
    public static Object[] getTestFixtures() {
        return new Object[] {
                new ElementLoaderTest(
                        "dev.getelements.elements.sdk.test.element.a",
                        VARIANT_A
                )
//                ,
//                new ElementLoaderTest(
//                        "dev.getelements.elements.sdk.test.element.b",
//                        VARIANT_B
//                )
        };
    }

    public ElementLoaderTest(
            final String elementPackage,
            final TestElementArtifact artifact) {
        this.elementPackage = elementPackage;
        this.elementRegistry = ElementRegistry.newDefaultInstance();
        this.elementUrl = testArtifactRegistry.findJarUrl(artifact);
    }

    @AfterClass
    public void closeElements() {
        elementRegistry.close();
    }

    private Element element;

    private String elementName;

    @Test
    public void testLoadThroughRegistry() {
        final var attributes = Attributes.emptyAttributes();

        final var elementUrls = new URL[] {elementUrl};

        final var loader = ElementLoaderFactory
                .getDefault()
                .getIsolatedLoader(attributes, baseClassLoader, cl -> new URLClassLoader(elementUrls, cl));

        element = elementRegistry.register(loader);
        elementName = element.getElementRecord().definition().name();

    }

    @Test(dependsOnMethods = "testLoadThroughRegistry")
    public void testGetElementByName() {
        final var element = elementRegistry.find(elementName).findFirst().get();
        Assert.assertEquals(element, this.element);
    }

    @Test(dependsOnMethods = "testLoadThroughRegistry")
    public void testGetTestService() {
        final var testService = element.getServiceLocator().getInstance(TestService.class);
        Assert.assertEquals(testService.getImplementationPackage(), elementPackage);
        Assert.assertEquals(testService.getClass().getPackageName(), elementPackage);
    }

    @Test(dependsOnMethods = "testGetTestService")
    public void testUseElementSupplierSpi() {
        final var testService = element.getServiceLocator().getInstance(TestService.class);
        testService.testElementSpi();
    }

    @Test(dependsOnMethods = "testGetTestService")
    public void testUseElementRegistrySupplierSpi() {
        final var testService = element.getServiceLocator().getInstance(TestService.class);
        testService.testElementRegistrySpi();
    }

    @Test(dependsOnMethods = "testUseElementRegistrySupplierSpi")
    public void testElementEvents() {

        final var testService = element.getServiceLocator().getInstance(TestService.class);
        final var eventObject1 = "testValue1";
        final var eventObject2 = "testValue2";

        final var event1 = Event.builder()
                .named(TEST_ELEMENT_EVENT_1)
                .argument(eventObject1)
                .argument(eventObject1)
                .build();

        final var event2 = Event.builder()
                .named(TEST_ELEMENT_EVENT_2)
                .argument(eventObject2)
                .argument(eventObject2)
                .build();

        elementRegistry.publish(event1);
        elementRegistry.publish(event2);

// TODO: Fix with EL-88
//Just the startup event
        Assert.assertEquals(testService.getConsumedEvents().size(), 1);
        Assert.assertEquals(testService.getConsumedEvents().getFirst().getEventName(), ElementLoader.SYSTEM_EVENT_ELEMENT_LOADED);
        Assert.assertEquals(testService.getConsumedEventObjects().size(), 2);
        Assert.assertEquals(testService.getConsumedEventObjects().get(0).name(), TEST_ELEMENT_EVENT_1);
        Assert.assertEquals(testService.getConsumedEventObjects().get(0).arguments().get(0), eventObject1);
        Assert.assertEquals(testService.getConsumedEventObjects().get(0).arguments().get(1), eventObject1);
        Assert.assertEquals(testService.getConsumedEventObjects().get(1).name(), TEST_ELEMENT_EVENT_2);
        Assert.assertEquals(testService.getConsumedEventObjects().get(1).arguments().get(0), eventObject2);
        Assert.assertEquals(testService.getConsumedEventObjects().get(1).arguments().get(1), eventObject2);

    }

}
