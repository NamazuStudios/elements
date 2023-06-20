//package dev.getelements.elements.cdnserve;
//
//import com.google.inject.TypeLiteral;
//import com.google.inject.servlet.ServletModule;
//import dev.getelements.elements.git.GitServletProvider;
//import dev.getelements.elements.git.HttpServletRepositoryResolver;
//import dev.getelements.elements.servlet.security.HttpServletBasicAuthFilter;
//import dev.getelements.elements.servlet.security.HttpServletGlobalSecretHeaderFilter;
//import org.eclipse.jgit.http.server.GitServlet;
//import org.eclipse.jgit.transport.resolver.RepositoryResolver;
//
//import javax.servlet.http.HttpServletRequest;
//
///**
// * Created by patricktwohig on 8/2/17.
// */
//public class CdnGitServletModule extends ServletModule {
//    @Override
//    protected void configureServlets() {
//
//        bind(HttpServletBasicAuthFilter.class).asEagerSingleton();
//        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
//        bind(GitServlet.class).toProvider(GitServletProvider.class).asEagerSingleton();
//        bind(new TypeLiteral<RepositoryResolver<HttpServletRequest>>(){}).to(HttpServletRepositoryResolver.class);
//
//        serve("/*").with(GitServlet.class);
//
//        filter("/*").through(HttpServletBasicAuthFilter.class);
//        filter("/*").through(HttpServletGlobalSecretHeaderFilter.class);
//
//    }
//}
