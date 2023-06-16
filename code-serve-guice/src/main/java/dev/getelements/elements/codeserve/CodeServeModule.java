//package dev.getelements.elements.codeserve;
//
//import com.google.inject.AbstractModule;
//import dev.getelements.elements.dao.mongo.guice.MongoCoreModule;
//import dev.getelements.elements.dao.mongo.guice.MongoDaoModule;
//import dev.getelements.elements.dao.mongo.guice.MongoSearchModule;
//import dev.getelements.elements.guice.ConfigurationModule;
//import dev.getelements.elements.rt.git.FilesystemGitLoaderModule;
//import dev.getelements.elements.rt.git.GitApplicationBootstrapperModule;
//import dev.getelements.elements.service.guice.RedissonClientModule;
//import ru.vyarus.guice.validator.ValidationModule;
//
//
//import java.util.Properties;
//import java.util.function.Supplier;
//
//public class CodeServeModule extends AbstractModule {
//
//    private final Supplier<Properties> configurationSupplier;
//
//    public CodeServeModule(final Supplier<Properties> configurationSupplier) {
//        this.configurationSupplier = configurationSupplier;
//    }
//
//    @Override
//    protected void configure() {
//        install(new ConfigurationModule(configurationSupplier));
//        install(new RedissonClientModule());
//        install(new ServicesModule());
//        install(new MongoCoreModule());
//        install(new MongoDaoModule());
//        install(new MongoSearchModule());
//        install(new ValidationModule());
//        install(new FilesystemGitLoaderModule());
//        install(new GitApplicationBootstrapperModule());
//        install(new FileSystemApplicationRepositoryResolverModule());
//    }
//
//}
