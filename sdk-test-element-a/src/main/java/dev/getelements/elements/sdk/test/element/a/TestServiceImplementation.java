package dev.getelements.elements.sdk.test.element.a;

import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.ElementSupplier;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.test.element.TestService;

public class TestServiceImplementation implements TestService {

    @ElementDefaultAttribute("test.value")
    public static final String TEST_CONFIGURATION_PARAMETER = "dev.getelements.elements.sdk.test.element.a.config";

    static {
        System.out.println();
    }

    @Override
    public String getImplementationPackage() {
        return getClass().getPackage().getName();
    }

    @Override
    public void testElementSpi() {
        final var element = ElementSupplier.getElementLocal(getClass()).get();
        final var elementName = element.getElementRecord().definition().name();
        assert elementName.equals(getImplementationPackage());
    }

    @Override
    public void testElementRegistrySpi() {
        final var element = ElementSupplier.getElementLocal(getClass()).get();
        final var elementRegistrySupplier = ElementRegistrySupplier.getElementLocal(TestServiceImplementation.class);
        final var elementRegistry = elementRegistrySupplier.get();
        assert element == elementRegistry.find(element.getElementRecord().definition().name()).findFirst().get();
    }

}
