//package dev.getelements.elements.sdk.spi;
//
//import dev.getelements.elements.sdk.ElementLoader;
//import dev.getelements.elements.sdk.ElementLoaderFactory;
//import dev.getelements.elements.sdk.PermittedTypes;
//import dev.getelements.elements.sdk.annotation.ElementSpiImplementation;
//
//@ElementSpiImplementation
//public class PermitElementLoader implements PermittedTypes {
//
//    @Override
//    public boolean test(final Class<?> aClass) {
//        return ElementLoader.class.isAssignableFrom(aClass) ||
//               ElementLoaderFactory.class.isAssignableFrom(aClass);
//    }
//
//}
