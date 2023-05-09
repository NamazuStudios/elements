package dev.getelements.elements.appserve.testkit;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class AppServeTestKitMainTest {

    @Test
    public void testUnitTestsAreSane() throws Exception{

//        final File examples = findExamples();
//        final File exampleTestSuite = new File(examples, "example-test-suite.txt")
//            .getAbsoluteFile()
//            .getCanonicalFile();
//
//        assertTrue(exampleTestSuite.isFile(), "No test suite at: " + exampleTestSuite);
//
//        AppServeTestKitMain.main(new String[]{
//            "--project-root", examples.getAbsolutePath(),
//            "--test-suite", exampleTestSuite.getAbsolutePath(),
//            "--integration", Boolean.FALSE.toString()
//        });

    }

    @Test
    public void testIntegrationTestsAreSane() throws Exception {

//        final File examples = findExamples();
//        final File exampleIntegrationTestSuite = new File(examples,"example-integration-test-suite.txt")
//            .getAbsoluteFile()
//            .getCanonicalFile();
//
//        assertTrue(exampleIntegrationTestSuite.isFile(), "No test suite at: " + exampleIntegrationTestSuite);
//
//        AppServeTestKitMain.main(new String[]{
//            "--project-root", examples.getAbsolutePath(),
//            "--test-suite", exampleIntegrationTestSuite.getAbsolutePath(),
//            "--integration", Boolean.FALSE.toString()
//        });

    }

    private File findExamples() throws Exception {

        // Maven and Intellij launch tests from different working directories, so it's easier just to figure out the
        // entry point on-the fly than to impose restrictions in either environment.  It is far more important that it
        // gets run.

        File examples = new File(".", "example");

        if (!examples.isDirectory()) {
            examples = new File(".", "app-serve-testkit/example");
        }

        examples = examples.getAbsoluteFile().getCanonicalFile();
        assertTrue(examples.isDirectory(), "Could not load example tests: " + examples);

        return examples;

    }

}
