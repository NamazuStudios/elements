package dev.getelements.elements.sdk.mongo.standard;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;

import javax.net.ssl.KeyManagerFactory;
import java.util.function.Function;

public class StandardKeyManagerFactoryAlgorithmSupplier implements Function<ElementDefaultAttribute, String> {
    @Override
    public String apply(final ElementDefaultAttribute attribute) {
        return KeyManagerFactory.getDefaultAlgorithm();
    }
}
