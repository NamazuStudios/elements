package com.namazustudios.socialengine.rt.testkit;

/**
 * Defines a simple unit test to run.
 */
public interface Test {

    /**
     * Returns the name of the test.
     *
     * @return the name of the test.
     */
    String getName();

    /**
     * Provides the name of the test module to run.
     *
     * @return the name of the module
     */
    String getModule();

    /**
     *
     * @return
     */
    String getTestMethod();

}
