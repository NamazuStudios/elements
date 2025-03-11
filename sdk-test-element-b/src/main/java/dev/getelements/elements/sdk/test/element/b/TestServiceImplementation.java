package dev.getelements.elements.sdk.test.element.b;

import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.ElementSupplier;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import dev.getelements.elements.sdk.test.element.TestService;

@ElementServiceImplementation
@ElementServiceExport(TestService.class)
public class TestServiceImplementation implements TestService {

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
