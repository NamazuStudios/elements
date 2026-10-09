package dev.getelements.elements.sdk.util;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.isExecutable;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class OperatorPropertiesTest {

    private static final String SYSTEM_PROPERTY_KEY = "dev.getelements.test.operator.properties.system";

    @AfterMethod
    public void clearProperty() {
        System.clearProperty(SYSTEM_PROPERTY_KEY);
    }

    @Test
    public void systemPropertyOverridesEnvironmentVariable() {
        System.setProperty(SYSTEM_PROPERTY_KEY, "from-system-property");
        final var properties = OperatorProperties.resolve();
        assertEquals(properties.getProperty(SYSTEM_PROPERTY_KEY), "from-system-property");
    }

    /**
     * Environment variables can't be set for the currently-running JVM, so this spawns a child process with
     * the variables under test set, mirroring {@code DefaultConfigurationSupplierTest#testRemapEnvironment}.
     */
    @Test
    public void resolvesAndTranslatesEnvironmentVariables() throws Exception {

        final var props = System.getProperties();

        final var javaHome = Paths.get(props.get("java.home").toString());
        final var classpath = props.get("java.class.path").toString();

        var jvmExecutable = javaHome.resolve("bin/java");

        if (!isExecutable(jvmExecutable)) {
            jvmExecutable = javaHome.resolve("bin/java.exe");
        }

        if (!isExecutable(jvmExecutable)) {
            fail("Unable to determine JVM executable: " + jvmExecutable);
        }

        final var processBuilder = new ProcessBuilder();
        processBuilder.environment().put("CLASSPATH", classpath);
        processBuilder.environment().put("dev_getelements_test_operator_dot", "dot");
        processBuilder.environment().put("ELEMENTS_TEST_OPERATOR_UPPERCASE", "uppercase");
        processBuilder.command(jvmExecutable.toString(), TestExecutable.class.getName());

        final var process = processBuilder.start();
        final var result = process.waitFor();

        final var stdout = new String(process.getInputStream().readAllBytes());
        assertEquals(result, 0, "Process output: " + stdout);

    }

    public static class TestExecutable {

        public static void main(final String[] args) {

            final var properties = OperatorProperties.resolve();
            final var dot = properties.get("dev.getelements.test.operator.dot");
            final var uppercase = properties.get("ELEMENTS_TEST_OPERATOR_UPPERCASE");

            assertEquals(dot, "dot");
            assertEquals(uppercase, "uppercase");

        }

    }

}
