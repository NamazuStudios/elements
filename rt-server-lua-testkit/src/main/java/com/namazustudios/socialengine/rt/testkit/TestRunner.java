package com.namazustudios.socialengine.rt.testkit;

public interface TestRunner {

    /**
     * A special logger which should be assigned INFO level to see the basic test reporting.
     */
    String LOGGER = "com.namazustudios.socialengine.rt.testkit.TestRunner.LOGGER";

    /**
     * Performs all tests in the suite.
     */
    void perform();

}
