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
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CachingShrinkwrapElementArtifactLoader implements ElementArtifactLoader {

    private static final Logger logger = LoggerFactory.getLogger(CachingShrinkwrapElementArtifactLoader.class);

    static {
        // ShrinkWrap's LogTransferListener logs MetadataNotFoundException at WARNING via JUL.
        // These failures are expected when Aether probes all configured repositories for SNAPSHOT
        // metadata — e.g. JitPack (added by elements that use JitPack libraries) will 404 for
        // dev.getelements.elements:sdk-spi and other non-JitPack artifacts.  The failures are
        // already caught and handled by isNotFound(); raising the log threshold to SEVERE
        // eliminates the noise. Genuine transfer/connection failures are surfaced separately by
        // this class at WARN through SLF4J (see resolveArtifacts), so the raised threshold does
        // not hide them.
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

    /**
     * Exception types indicating that a configured repository could not be contacted, as opposed to the artifact
     * simply not existing. Matched by class name along the cause chain, like {@link #NOT_FOUND_EXCEPTIONS}. Note
     * that {@code ArtifactNotFoundException} is a subclass of {@code ArtifactTransferException}, but class-name
     * matching on exact types distinguishes them, and {@link #isUnreachable(Throwable)} is checked first so a
     * mixed failure (404 from one repository, connection refused from another) is treated as unreachable.
     */
    private static final Set<String> UNREACHABLE_EXCEPTIONS = Set.of(
        "org.eclipse.aether.transfer.ArtifactTransferException",
        "org.eclipse.aether.transfer.MetadataTransferException",
        "org.eclipse.aether.transfer.NoRepositoryConnectorException",
        "org.eclipse.aether.transfer.RepositoryOfflineException",
        "java.net.ConnectException",
        "java.net.NoRouteToHostException",
        "java.net.SocketException",
        "java.net.SocketTimeoutException",
        "java.net.UnknownHostException"
    );

    private static final Set<ScopeType> PERMITTED_SCOPES = Set.of(ScopeType.COMPILE, ScopeType.RUNTIME);

    @Override
    public Optional<ClassLoader> findClassLoader(final ClassLoader parent,
                                                 final Set<ArtifactRepository> repositories,
                                                 final Set<String> coordinates) {

        final MavenResolvedArtifact[] artifacts;

        try {
            artifacts = resolveArtifacts(
                    repositories, coordinates, resolve -> resolve.withTransitivity().asResolvedArtifact());
        } catch (ArtifactNotResolved ex) {
            return Optional.empty();
        }

        final var files = Stream.of(artifacts)
                .filter(a -> !a.isOptional())
                .filter(a -> PERMITTED_SCOPES.contains(a.getScope()))
                .map(ResolvedArtifact::asFile)
                    .toArray(File[]::new);

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
            resolvedArtifacts = resolveArtifacts(
                    repositories, List.of(coordinates), resolve -> resolve.withTransitivity().asResolvedArtifact());
        } catch (ArtifactNotResolved ex) {
            throw new SdkArtifactNotFoundException(ex.getCause());
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
            resolvedArtifact = resolveArtifacts(repositories, List.of(coordinates),
                    resolve -> resolve.withoutTransitivity().asSingleResolvedArtifact());
        } catch (ArtifactNotResolved ex) {
            return Optional.empty();
        }

        return Optional.of(toArtifact(resolvedArtifact));

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

    /**
     * Resolves the given coordinates against the configured repositories, translating every failure into one of
     * three outcomes: the extracted result on success; an {@link ArtifactNotResolved} signal when the artifact
     * does not exist; or an {@link SdkException} carrying the underlying cause and the configured repositories
     * when resolution fails for any other reason.
     * <p>
     * When resolution fails because a repository is unreachable (connection refused, DNS failure, timeout, a
     * missing repository connector), the resolution is retried fully offline against the local Maven repository,
     * and the cached copy is used when present (#148). Aether's enhanced local repository manager gates cache
     * hits on the availability of the repository recorded in {@code _remote.repositories}, so the retry also
     * switches to the legacy local repository manager, which considers any artifact present on disk available
     * regardless of where it was originally downloaded from.
     *
     * @param repositories the configured repositories
     * @param coordinates  the coordinates to resolve
     * @param extract      applies the desired transitivity/shape to the strategy stage
     * @param <T>          the resolved shape ({@code MavenResolvedArtifact[]} or {@code MavenResolvedArtifact})
     * @return the extracted result
     */
    private <T> T resolveArtifacts(final Set<ArtifactRepository> repositories,
                                   final Collection<String> coordinates,
                                   final Function<MavenStrategyStage, T> extract) {

        final var joined = String.join(",", coordinates);

        try {
            return extract.apply(resolveStage(repositories, coordinates, false));
        } catch (RuntimeException onlineFailure) {

            // An unreachable repository is not the same as a missing artifact: fall back to whatever the local
            // Maven repository already holds before giving up (#148).
            if (isUnreachable(onlineFailure)) {

                logger.warn(
                        "Artifact repository unreachable while resolving [{}]; trying the locally cached copy. "
                                + "Configured repositories: [{}]",
                        joined,
                        describeRepositories(repositories),
                        onlineFailure
                );

                try {
                    final var cached = extract.apply(resolveStage(repositories, coordinates, true));
                    logger.warn("Resolved [{}] from the local Maven repository.", joined);
                    return cached;
                } catch (RuntimeException offlineFailure) {
                    throw new SdkException(
                            "Unable to resolve artifact coordinates [" + joined
                                    + "]: a configured repository is unreachable (" + rootCause(onlineFailure)
                                    + ") and the artifacts are not available in the local Maven repository ("
                                    + rootCause(offlineFailure)
                                    + "). Configured repositories: [" + describeRepositories(repositories) + "]",
                            onlineFailure);
                }

            }

            if (isNotFound(onlineFailure)) {
                logger.info("Unable to resolve artifact coordinates: [{}]", joined, onlineFailure);
                throw new ArtifactNotResolved(onlineFailure);
            }

            throw new SdkException(
                    "Unable to resolve artifact coordinates [" + joined + "]. Configured repositories: ["
                            + describeRepositories(repositories) + "]",
                    onlineFailure);

        }
    }

    /**
     * Builds the resolution for the given repositories, optionally restricted to the local Maven repository.
     *
     * @param repositories the configured repositories
     * @param coordinates  the coordinates to resolve
     * @param offline      true to resolve without contacting any repository, ignoring
     *                     {@code _remote.repositories} tracking
     * @return the strategy stage for the requested resolution
     */
    private MavenStrategyStage resolveStage(final Set<ArtifactRepository> repositories,
                                            final Collection<String> coordinates,
                                            final boolean offline) {

        final var config = configurableSystem(repositories);

        if (offline) {
            return config.workOffline().useLegacyLocalRepo(true).resolve(coordinates);
        }

        return config.resolve(coordinates);

    }

    private static String describeRepositories(final Set<ArtifactRepository> repositories) {

        if (repositories == null || repositories.isEmpty()) {
            return "none";
        }

        return repositories.stream()
                .map(r -> r.isDefault() ? "central(default)" : r.id() + "(" + r.url() + ")")
                .sorted()
                .collect(Collectors.joining(", "));

    }

    private static Throwable rootCause(final Throwable throwable) {
        var cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static boolean chainContains(final Throwable throwable, final Set<String> classNames) {

        final var seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());

        for (var cause = throwable; cause != null && seen.add(cause); cause = cause.getCause()) {
            if (classNames.contains(cause.getClass().getName())) {
                return true;
            }
        }

        return false;

    }

    private static boolean isNotFound(final Throwable throwable) {
        return chainContains(throwable, NOT_FOUND_EXCEPTIONS);
    }

    private static boolean isUnreachable(final Throwable throwable) {
        return chainContains(throwable, UNREACHABLE_EXCEPTIONS);
    }

    /**
     * Internal signal that resolution failed because the artifact does not exist, letting each
     * {@link ElementArtifactLoader} method translate that outcome into its own contract.
     */
    private static final class ArtifactNotResolved extends RuntimeException {

        private ArtifactNotResolved(final Throwable cause) {
            super(cause);
        }

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
