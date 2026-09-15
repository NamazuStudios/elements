package dev.getelements.elements.sdk.spi.shrinkwrap;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.exception.SdkArtifactNotFoundException;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.Artifact;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import org.jboss.shrinkwrap.resolver.api.ResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class CachingShrinkwrapElementArtifactLoader implements ElementArtifactLoader {

    private static final Logger logger = LoggerFactory.getLogger(CachingShrinkwrapElementArtifactLoader.class);

    static {
        // ShrinkWrap's LogTransferListener logs MetadataNotFoundException at WARNING via JUL.
        // These failures are expected when Aether probes all configured repositories for SNAPSHOT
        // metadata — e.g. JitPack (added by elements that use JitPack libraries) will 404 for
        // dev.getelements.elements:sdk-spi and other non-JitPack artifacts.  The failures are
        // already caught and handled by isNotFound(); raising the log threshold to SEVERE
        // eliminates the noise without hiding real problems.
        java.util.logging.Logger
                .getLogger("org.jboss.shrinkwrap.resolver.impl.maven.logging.LogTransferListener")
                .setLevel(java.util.logging.Level.SEVERE);
        applyLocalRepositoryOverride();
    }

    public static final String DEFAULT_LAYOUT = "default";

    /**
     * System property checked (before {@link #LOCAL_REPOSITORY_ENV_VAR}) to redirect the Maven/Aether
     * local-repository cache -- e.g. to an NFS-mounted, cluster-shared directory so multiple server
     * instances avoid redundant artifact resolution/downloads.
     */
    public static final String LOCAL_REPOSITORY_PROPERTY = "dev.getelements.elements.maven.repo.local";

    /**
     * Environment variable checked if {@link #LOCAL_REPOSITORY_PROPERTY} isn't set.
     */
    public static final String LOCAL_REPOSITORY_ENV_VAR = "ELEMENTS_MAVEN_REPO_LOCAL";

    /**
     * The system property ShrinkWrap's own resolver reads for the local-repository path
     * ({@code MavenSettingsBuilder.ALT_LOCAL_REPOSITORY_LOCATION}). Not configurable via an environment
     * variable in ShrinkWrap itself -- {@link #applyLocalRepositoryOverride()} bridges
     * {@link #LOCAL_REPOSITORY_PROPERTY}/{@link #LOCAL_REPOSITORY_ENV_VAR} into it.
     */
    private static final String SHRINKWRAP_LOCAL_REPOSITORY_PROPERTY = "maven.repo.local";

    private static final Set<String> NOT_FOUND_EXCEPTIONS = Set.of(
        "org.eclipse.aether.transfer.ArtifactNotFoundException",
        "org.eclipse.aether.transfer.MetadataNotFoundException"
    );

    private static final Set<ScopeType> PERMITTED_SCOPES = Set.of(ScopeType.COMPILE, ScopeType.RUNTIME);

    @Override
    public Optional<ClassLoader> findClassLoader(final ClassLoader parent,
                                                 final Set<ArtifactRepository> repositories,
                                                 final Set<String> coordinates) {

        final File[] files;

        try {

            final var artifacts = configurableSystem(repositories)
                    .resolve(coordinates)
                    .withTransitivity()
                    .asResolvedArtifact();

            files = Stream.of(artifacts)
                    .filter(a -> !a.isOptional())
                    .filter(a -> PERMITTED_SCOPES.contains(a.getScope()))
                    .map(ResolvedArtifact::asFile)
                        .toArray(File[]::new);

        } catch (RuntimeException ex) {
            if (isNotFound(ex)) {

                logger.info("Unable to resolve artifact coordinates: [{}]",
                        String.join(",", coordinates),
                        ex
                );

                return Optional.empty();

            } else {
                throw new SdkException(ex);
            }
        }

        final var classpath = Stream.of(files)
                .map(f -> {
                    try { return f.toURL(); }
                    catch (MalformedURLException ex) { throw new SdkException(ex); }
                })
                .toArray(URL[]::new);

        return Optional.of(new URLClassLoader(classpath, parent));

    }

    @Override
    public Stream<Artifact> findClasspathForArtifact(final Set<ArtifactRepository> repositories,
                                                     final String coordinates) {

        final MavenResolvedArtifact[] resolvedArtifacts;

        try {
            resolvedArtifacts = configurableSystem(repositories)
                    .resolve(coordinates)
                    .withTransitivity()
                    .asResolvedArtifact();
        } catch (RuntimeException ex) {
            if (isNotFound(ex)) {

                logger.info("Unable to resolve artifact coordinates: [{}]",
                        String.join(",", coordinates),
                        ex
                );

                throw new SdkArtifactNotFoundException(ex);

            } else {
                throw new SdkException(ex);
            }
        }

        return Stream
                .of(resolvedArtifacts)
                .filter(a -> !a.isOptional())
                .filter(a -> PERMITTED_SCOPES.contains(a.getScope()))
                .map(CachingShrinkwrapElementArtifactLoader::toArtifact);

    }

    @Override
    public Optional<Artifact> findArtifact(final Set<ArtifactRepository> repositories, final String coordinates) {

        final MavenResolvedArtifact resolvedArtifact;

        try {
            resolvedArtifact = configurableSystem(repositories)
                    .resolve(coordinates)
                    .withoutTransitivity()
                    .asSingleResolvedArtifact();
        } catch (RuntimeException ex) {
            if (isNotFound(ex)) {

                logger.info("Unable to resolve artifact coordinates: [{}]",
                        String.join(",", coordinates),
                        ex
                );

                return Optional.empty();

            } else {
                throw new SdkException(ex);
            }
        }

        final var artifact = toArtifact(resolvedArtifact);
        return Optional.of(artifact);

    }

    /**
     * Promotes {@value #LOCAL_REPOSITORY_PROPERTY} / {@value #LOCAL_REPOSITORY_ENV_VAR} into ShrinkWrap's
     * own {@value #SHRINKWRAP_LOCAL_REPOSITORY_PROPERTY} system property, so operators can redirect the
     * Maven/Aether local-repository cache via this platform's normal env-var/property convention instead
     * of an undocumented raw JVM flag. Never overrides an already-explicit
     * {@value #SHRINKWRAP_LOCAL_REPOSITORY_PROPERTY} system property -- an operator who set that directly
     * is respected as-is.
     *
     * <p>Public (not {@code private}) so it can be invoked directly from tests, since the {@code static}
     * initializer that calls it in normal operation only runs once per JVM.</p>
     */
    public static void applyLocalRepositoryOverride() {

        if (System.getProperty(SHRINKWRAP_LOCAL_REPOSITORY_PROPERTY) != null) {
            return;
        }

        final var override = System.getProperty(LOCAL_REPOSITORY_PROPERTY, System.getenv(LOCAL_REPOSITORY_ENV_VAR));

        if (override != null && !override.isBlank()) {
            System.setProperty(SHRINKWRAP_LOCAL_REPOSITORY_PROPERTY, override.trim());
            logger.info("Using configured Maven local repository: {}", override.trim());
        }

    }

    private ConfigurableMavenResolverSystem configurableSystem(final Set<ArtifactRepository> repositories) {

        var config = Maven.configureResolver().withClassPathResolution(false);

        for (var repository : repositories) {
            if (repository.isDefault()) {
                config = config.withMavenCentralRepo(true);
            } else {
                config = config.withRemoteRepo(repository.id(), repository.url(), DEFAULT_LAYOUT);
            }
        }

        return config;

    }

    private static boolean isNotFound(Throwable t) {

        for (Throwable c = t; c != null; c = c.getCause()) {

            final var name = c.getClass().getName();

            if (NOT_FOUND_EXCEPTIONS.contains(name)) {
                return true;
            }

        }

        return false;

    }

    private static Artifact toArtifact(final MavenResolvedArtifact mavenResolvedArtifact) {
        final var coordinate = mavenResolvedArtifact.getCoordinate();
        return new Artifact(
                mavenResolvedArtifact.asFile().toPath(),
                coordinate.getGroupId(),
                coordinate.getArtifactId(),
                coordinate.getVersion(),
                coordinate.getClassifier(),
                coordinate.getPackaging().toString(),
                mavenResolvedArtifact.getExtension()
        );
    }

}
