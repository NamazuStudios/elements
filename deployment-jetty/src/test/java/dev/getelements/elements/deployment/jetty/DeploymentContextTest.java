package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.Test;

import java.nio.file.Files;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Covers {@link DeploymentContext#createDeploymentDirectory()}, which {@code StandardElementRuntimeService}
 * relies on to clean up staged element directories on close/failure (see {@code ActiveDeployment#close}).
 * Confirms the created directory is a real on-disk directory tracked in both {@code elementPaths()}
 * (consumed by the loader) and {@code deploymentDirectories()} (consumed by cleanup), and that the same
 * {@link TemporaryFiles#deleteRecursively(java.nio.file.Path)} primitive that cleanup now calls actually
 * removes it. This does not exercise {@code StandardElementRuntimeService} itself (that would need a full
 * Guice/artifact-loader fixture); it verifies the tracking + deletion primitives that fix relies on.
 */
public class DeploymentContextTest {

    @Test
    public void createDeploymentDirectoryTracksRealDirectoryForCleanup() {

        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn("deployment-1");
        when(deployment.useDefaultRepositories()).thenReturn(false);
        when(deployment.repositories()).thenReturn(null);

        final var context = DeploymentContext.create(
                deployment,
                mock(MutableElementRegistry.class),
                mock(ElementArtifactLoader.class),
                mock(ElementPathLoader.class),
                new TemporaryFiles(DeploymentContextTest.class),
                Attributes.emptyAttributes()
        );

        final var deploymentDir = context.createDeploymentDirectory();

        assertTrue(Files.isDirectory(deploymentDir), "createDeploymentDirectory should create a real directory");
        assertTrue(context.elementPaths().contains(deploymentDir),
                "directory must be visible to the loader via elementPaths()");
        assertTrue(context.deploymentDirectories().contains(deploymentDir),
                "directory must be tracked for cleanup via deploymentDirectories()");

        // The exact cleanup primitive StandardElementRuntimeService.ActiveDeployment#close (and the
        // load-failure path in doLoadDeployment) now calls for every tracked deployment directory.
        TemporaryFiles.deleteRecursively(deploymentDir);

        assertFalse(Files.exists(deploymentDir), "cleanup should remove the staged directory from disk");

    }

}
