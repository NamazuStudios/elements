package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Regression coverage for #103: two packages in one deployment that reference the same ELM artifact must each
 * stage as an independent mount. {@link DeploymentContext#stageElmPackage} copies the resolved artifact to a
 * unique per-package temp file before mounting, so identical coordinates can never collide on the same
 * underlying file. Before the fix the package path mounted the resolved artifact path directly, so the second
 * package was coupled to the first package's shared backing file and the deployment degraded silently.
 */
public class StandardElementRuntimeServiceElmStageCollisionTest {

    @Test
    public void sameElmArtifactStagesTwoIndependentMounts() throws Exception {

        final var elmPath = createElmFixture("fixture.elm");

        final var context = DeploymentContext.create(
                deployment(),
                mock(MutableElementRegistry.class),
                mock(ElementArtifactLoader.class),
                mock(ElementPathLoader.class),
                new TemporaryFiles(StandardElementRuntimeServiceElmStageCollisionTest.class),
                Attributes.emptyAttributes()
        );

        // Simulate two package definitions that resolve the same artifact to the same real path: stage twice.
        final var firstRoot = context.stageElmPackage(elmPath, "test:elm:1.0");
        final var secondRoot = context.stageElmPackage(elmPath, "test:elm:1.0");

        assertEquals(context.elementPaths().size(), 2, "both packages must stage");
        assertTrue(context.errors().isEmpty(),
                "same-artifact staging must not produce errors: " + context.errors());

        assertEquals(context.elementPaths().get(0), firstRoot);
        assertEquals(context.elementPaths().get(1), secondRoot);

        // Each package got its own file system and mount, not a shared one.
        assertTrue(firstRoot.getFileSystem() != secondRoot.getFileSystem(),
                "each package must mount its own file system");
        assertEquals(
                new HashSet<>(context.fileSystems()),
                new HashSet<>(java.util.List.of(firstRoot.getFileSystem(), secondRoot.getFileSystem())),
                "both file systems must be tracked for cleanup"
        );

        // The whole point of the fix: each package stages to its own tracked temporary copy of the artifact,
        // so identical coordinates can never collide on the same underlying file (the JDK zip provider
        // only guarantees independent mounts for distinct paths; duplicate mounts are provider-dependent).
        assertEquals(context.deploymentFiles().size(), 2,
                "each package must stage to its own tracked temporary copy");
        assertTrue(context.deploymentFiles().stream().noneMatch(p -> p.equals(elmPath)),
                "mounts must be backed by fresh temp copies, never the shared artifact path");
        assertTrue(!firstRoot.getFileSystem().toString().equals(elmPath.toString())
                        && !secondRoot.getFileSystem().toString().equals(elmPath.toString()),
                "each zip file system must be backed by its own temp copy, not the artifact");

    }

    private static ElementDeployment deployment() {
        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn("deployment-1");
        when(deployment.useDefaultRepositories()).thenReturn(false);
        when(deployment.repositories()).thenReturn(java.util.List.of());
        return deployment;
    }

    private static Path createElmFixture(final String fileName) throws Exception {
        final Path path = Files.createTempFile("elm-fixture-", fileName.substring(fileName.indexOf('.')));
        try (final OutputStream out = Files.newOutputStream(path)) {
            try (final var zip = new ZipOutputStream(out)) {
                zip.putNextEntry(new ZipEntry("element/"));
                zip.closeEntry();
                zip.putNextEntry(new ZipEntry("element/manifest.json"));
                zip.write("{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return path;
    }

}