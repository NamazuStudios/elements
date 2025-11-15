package dev.getelements.elements.sdk.mongo.standard;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;

import javax.net.ssl.TrustManagerFactory;
import java.util.function.Function;

public class StandardTrustManagerFactoryAlgorithmSupplier implements Function<ElementDefaultAttribute, String> {
    @Override
    public String apply(ElementDefaultAttribute attribute) {
        return TrustManagerFactory.getDefaultAlgorithm();

    }
}
