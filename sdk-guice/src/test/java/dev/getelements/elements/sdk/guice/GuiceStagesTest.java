package dev.getelements.elements.sdk.guice;

import com.google.inject.Stage;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;

import static dev.getelements.elements.sdk.guice.GuiceStages.STAGE_ENV_VAR;
import static dev.getelements.elements.sdk.guice.GuiceStages.STAGE_PROPERTY;
import static java.nio.file.Files.isExecutable;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class GuiceStagesTest {

    @AfterMethod
    public void clearProperty() {
        System.clearProperty(STAGE_PROPERTY);
    }

    @Test
    public void defaultsToDevelopmentWhenUnset() {
        assertEquals(GuiceStages.get(), Stage.DEVELOPMENT);
    }

    @Test
    public void honorsSystemProperty() {
        System.setProperty(STAGE_PROPERTY, "PRODUCTION");
        assertEquals(GuiceStages.get(), Stage.PRODUCTION);
    }

    @Test
    public void systemPropertyIsCaseInsensitive() {
        System.setProperty(STAGE_PROPERTY, "production");
        assertEquals(GuiceStages.get(), Stage.PRODUCTION);
    }

    @Test
    public void fallsBackToDevelopmentOnInvalidValue() {
        System.setProperty(STAGE_PROPERTY, "not-a-real-stage");
        assertEquals(GuiceStages.get(), Stage.DEVELOPMENT);
    }

    @Test
    public void fallsBackToDevelopmentOnBlankValue() {
        System.setProperty(STAGE_PROPERTY, "   ");
        assertEquals(GuiceStages.get(), Stage.DEVELOPMENT);
    }

    /**
     * The environment variable can't be set for the currently-running JVM, so this spawns a child process
     * with it set, mirroring {@code OperatorPropertiesTest}. Confirms GuiceStages actually reaches
     * {@link #STAGE_ENV_VAR} via {@code OperatorProperties} rather than only ever seeing the system property.
     */
    @Test
    public void honorsEnvironmentVariable() throws Exception {

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
        processBuilder.environment().put(STAGE_ENV_VAR, "PRODUCTION");
        processBuilder.command(jvmExecutable.toString(), TestExecutable.class.getName());

        final var process = processBuilder.start();
        final var result = process.waitFor();

        final var stdout = new String(process.getInputStream().readAllBytes());
        assertEquals(result, 0, "Process output: " + stdout);

    }

    public static class TestExecutable {

        public static void main(final String[] args) {
            assertEquals(GuiceStages.get(), Stage.PRODUCTION);
        }

    }

}
