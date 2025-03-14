package dev.getelements.elements.sdk.test;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.test.element.TestService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_A;
import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_B;

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
                ),
                new ElementLoaderTest(
                        "dev.getelements.elements.sdk.test.element.b",
                        VARIANT_B
                )
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
        final var eventObject = "testValue";
        final var event = Event.builder()
                .named(TestService.TEST_ELEMENT_EVENT)
                .argument(eventObject)
                .argument(eventObject)
                .build();

        elementRegistry.publish(event);

        //Includes the startup event and the event published above
        Assert.assertEquals(testService.getConsumedEvents().size(), 2);
        Assert.assertEquals(testService.getConsumedEventObjects().size(), 2);
        Assert.assertEquals(testService.getConsumedEventObjects().get(1), eventObject);
    }

}
