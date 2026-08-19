package dev.getelements.elements.sdk.test.element.mongo;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import dev.getelements.elements.sdk.dao.EntityRegistry;

import java.util.List;

@ElementServiceImplementation
@ElementServiceExport(EntityRegistry.class)
public class TestRedeployEntityRegistry implements EntityRegistry {

    @Override
    public List<Class<?>> entityClasses() {
        return List.of(TestRedeployDocument.class);
    }

}
