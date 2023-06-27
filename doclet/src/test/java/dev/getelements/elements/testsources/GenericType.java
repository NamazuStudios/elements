package dev.getelements.elements.testsources;

/**
 * A generic type. Exactly what it sounds.
 */
public class GenericType<ParameterT> {

    /**
     * A generic method
     *
     * @param <T> the type parameter
     *
     * @return null always
     */
    public <T> T genericMethod() {
        return null;
    }

}
