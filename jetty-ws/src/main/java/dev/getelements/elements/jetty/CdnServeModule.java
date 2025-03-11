package dev.getelements.elements.jetty;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.cdnserve.LargeObjectServlet;
import dev.getelements.elements.cdnserve.StaticContentServlet;
import dev.getelements.elements.cdnserve.guice.FileSystemCdnGitStorageModule;
import dev.getelements.elements.git.GitServletProvider;
import dev.getelements.elements.git.HttpServletRepositoryResolver;
import dev.getelements.elements.guice.ServletBindings;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import static com.google.inject.name.Names.named;

public class CdnServeModule extends PrivateModule {

    public static final String GIT_ROOT = "/cdn/git/*";

    public static final String OBJECT_ROOT = "/cdn/object/*";

    public static final String STATIC_CONTENT_ROOT = "/cdn/static/app/*";

    public static final String GIT_SERVLET_NAME = "dev.getelements.elements.cdn.git.servlet";

    public static final Key<GitServlet> GIT_SERVLET_KEY = Key.get(GitServlet.class, named(GIT_SERVLET_NAME));

    public static final Key<LargeObjectServlet> LARGE_OBJECT_SERVLET_KEY = Key.get(LargeObjectServlet.class);

    public static final Key<StaticContentServlet> STATIC_CONTENT_SERVLET_KEY = Key.get(StaticContentServlet.class);

    @Override
    protected void configure() {

        install(new FileSystemCdnGitStorageModule());

        bind(GIT_SERVLET_KEY)
                .toProvider(GitServletProvider.class)
                .asEagerSingleton();

        bind(new TypeLiteral<RepositoryResolver<HttpServletRequest>>(){})
                .to(HttpServletRepositoryResolver.class);

        bind(LARGE_OBJECT_SERVLET_KEY)
                .asEagerSingleton();

        bind(STATIC_CONTENT_SERVLET_KEY)
                .asEagerSingleton();

        expose(GIT_SERVLET_KEY);
        expose(LARGE_OBJECT_SERVLET_KEY);
        expose(STATIC_CONTENT_SERVLET_KEY);

    }

    public void accept(final ServletBindings bindings) {

        bindings.useBasicAuthFor(GIT_ROOT);
        bindings.useGlobalAuthFor(OBJECT_ROOT);
        bindings.useStandardAuthFor(OBJECT_ROOT);
        bindings.useGlobalAuthFor(STATIC_CONTENT_ROOT);

        bindings.serve(GIT_ROOT).with(GIT_SERVLET_KEY);
        bindings.serve(OBJECT_ROOT).with(LARGE_OBJECT_SERVLET_KEY);
        bindings.serve(STATIC_CONTENT_ROOT).with(STATIC_CONTENT_SERVLET_KEY);

    }

}
