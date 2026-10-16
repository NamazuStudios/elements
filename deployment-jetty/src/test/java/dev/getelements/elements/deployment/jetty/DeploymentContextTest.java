package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
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

    @Test
    public void defaultRepositoriesIncludeNamazuStudios() {

        // The system's default repository set is central plus the Namazu Studios Maven repository (#163),
        // so deployments resolve platform-published artifacts without configuring the repository in
        // every environment.
        assertEquals(ArtifactRepository.DEFAULTS, Set.of(
                ArtifactRepository.DEFAULT,
                ArtifactRepository.NAMAZU_STUDIOS
        ));

        assertFalse(ArtifactRepository.NAMAZU_STUDIOS.isDefault(), "the Namazu repository is a remote repository");
        assertEquals(ArtifactRepository.NAMAZU_STUDIOS.id(), "namazu");
        assertEquals(ArtifactRepository.NAMAZU_STUDIOS.url(), "https://maven.namazustudios.com/");

    }

    @Test
    public void useDefaultRepositoriesIncludesCentralAndNamazu() {

        final var context = createContext(true, null);

        assertTrue(context.repositories().contains(ArtifactRepository.DEFAULT), "central must be included");
        assertTrue(context.repositories().contains(ArtifactRepository.NAMAZU_STUDIOS),
                "the Namazu Studios repository must be included by default (#163)");
        assertEquals(context.repositories().size(), 2, "no other repositories may appear implicitly");

    }

    @Test
    public void notUsingDefaultRepositoriesIncludesNeither() {

        final var context = createContext(false, null);

        assertTrue(context.repositories().isEmpty(),
                "without defaults and without explicit repositories, none may be configured");

    }

    @Test
    public void explicitRepositoriesAreIncludedAlongsideDefaults() {

        final var custom = new ArtifactRepository("custom", "https://maven.example.com/");

        final var context = createContext(true, List.of(toElementArtifactRepository(custom)));

        assertTrue(context.repositories().contains(ArtifactRepository.DEFAULT));
        assertTrue(context.repositories().contains(ArtifactRepository.NAMAZU_STUDIOS));
        assertTrue(context.repositories().contains(custom), "the explicitly configured repository must be included");

    }

    @Test
    public void explicitRepositoriesWithoutDefaultsAreUsedAlone() {

        final var custom = new ArtifactRepository("custom", "https://maven.example.com/");

        final var context = createContext(false, List.of(toElementArtifactRepository(custom)));

        assertEquals(context.repositories(), Set.of(custom),
                "without defaults, only the explicitly configured repositories may be used");

    }

    @Test
    public void explicitlyConfiguredNamazuRepositoryIsNotDuplicated() {

        // An operator who already configured the Namazu repository explicitly must not end up with a
        // duplicate entry once it is part of the defaults.
        final var explicit = toElementArtifactRepository(ArtifactRepository.NAMAZU_STUDIOS);

        final var context = createContext(true, List.of(explicit));

        assertEquals(context.repositories().size(), 2, "the duplicate must be suppressed by the set");
        assertTrue(context.repositories().contains(ArtifactRepository.NAMAZU_STUDIOS));

    }

    @Test
    public void sameUrlUnderDifferentIdIsKeptAsConfigured() {

        // A record with the Namazu URL but a different id is a distinct repository as far as the
        // deployment is concerned and must be preserved as configured (not silently collapsed).
        final var custom = new ArtifactRepository("custom", ArtifactRepository.NAMAZU_STUDIOS.url());

        final var context = createContext(true, List.of(toElementArtifactRepository(custom)));

        assertTrue(context.repositories().contains(custom));
        assertTrue(context.repositories().contains(ArtifactRepository.NAMAZU_STUDIOS));
        assertEquals(context.repositories().size(), 3);

    }

    private DeploymentContext createContext(final boolean useDefaultRepositories,
                                            final List<dev.getelements.elements.sdk.model.system.ElementArtifactRepository> repositories) {

        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn("deployment-1");
        when(deployment.useDefaultRepositories()).thenReturn(useDefaultRepositories);
        when(deployment.repositories()).thenReturn(repositories);

        return DeploymentContext.create(
                deployment,
                mock(MutableElementRegistry.class),
                mock(ElementArtifactLoader.class),
                mock(ElementPathLoader.class),
                new TemporaryFiles(DeploymentContextTest.class),
                Attributes.emptyAttributes()
        );

    }

    private static dev.getelements.elements.sdk.model.system.ElementArtifactRepository toElementArtifactRepository(
            final ArtifactRepository repository) {
        return new dev.getelements.elements.sdk.model.system.ElementArtifactRepository(
                repository.id(), repository.url());
    }

}
