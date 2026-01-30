package dev.getelements.elements.sdk.spi.shrinkwrap;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class CachingShrinkwrapElementArtifactLoader implements ElementArtifactLoader {

    private static final Logger logger = LoggerFactory.getLogger(CachingShrinkwrapElementArtifactLoader.class);

    public static final String DEFAULT_LAYOUT = "default";

    private static final Set<String> NOT_FOUND_EXCEPTIONS = Set.of(
        "org.eclipse.aether.transfer.ArtifactNotFoundException",
        "org.eclipse.aether.transfer.MetadataNotFoundException"
    );

    @Override
    public Optional<ClassLoader> tryGetClassLoader(final ClassLoader parent,
                                                   final Set<ArtifactRepository> repositories,
                                                   final Set<String> coordinates) {

        final File[] files;

        try {

            files = configurableSystem(repositories)
                    .resolve(coordinates)
                    .withoutTransitivity()
                    .asFile();

        } catch (RuntimeException ex) {
            if (isNotFound(ex)) {
                logger.info("Unable to resolve artifact coordinates: {}", coordinates, ex);
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

    private ConfigurableMavenResolverSystem configurableSystem(final Set<ArtifactRepository> repositories) {

        var config = Maven.configureResolver();

        for (var repository : repositories) {

            if (repository.isDefault()) {
                config = config.withMavenCentralRepo(true);
            } else {
                config = config.withRemoteRepo(repository.id(), repository.url(), DEFAULT_LAYOUT);
            }

        }

        return config;

    }

    public static boolean isNotFound(Throwable t) {

        for (Throwable c = t; c != null; c = c.getCause()) {

            final var name = c.getClass().getName();

            if (NOT_FOUND_EXCEPTIONS.contains(name)) {
                return true;
            }

        }

        return false;

    }

}
