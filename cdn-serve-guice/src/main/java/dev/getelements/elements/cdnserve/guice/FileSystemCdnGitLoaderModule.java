package dev.getelements.elements.cdnserve.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.git.GitApplicationAssetLoader;
import dev.getelements.elements.cdnserve.FilesystemCdnGitLoaderProvider;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.service.cdn.CdnDeploymentService.GIT_REPO;

public class FileSystemCdnGitLoaderModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GitApplicationAssetLoader.class)
                .annotatedWith(named(GIT_REPO))
                .toProvider(FilesystemCdnGitLoaderProvider.class)
                .asEagerSingleton();
    }

}
