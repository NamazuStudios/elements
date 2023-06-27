package dev.getelements.elements.rt.testkit;

public interface TestRunner {

    /**
     * A special logger which should be assigned INFO level to see the basic test reporting.
     */
    String LOGGER = "dev.getelements.elements.rt.testkit.TestRunner.LOGGER";

    /**
     * Performs all tests in the suite.
     */
    void perform();

}
