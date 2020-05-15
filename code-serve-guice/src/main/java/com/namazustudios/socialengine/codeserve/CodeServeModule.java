package com.namazustudios.socialengine.codeserve;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.dao.rt.guice.RTGitBootstrapModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import org.apache.bval.guice.ValidationModule;

import java.util.Properties;
import java.util.function.Supplier;

public class CodeServeModule extends AbstractModule {

    private final Supplier<Properties> configurationSupplier;

    public CodeServeModule(final Supplier<Properties> configurationSupplier) {
        this.configurationSupplier = configurationSupplier;
    }

    @Override
    protected void configure() {
        install(new ConfigurationModule(configurationSupplier));
        install(new RedisModule());
        install(new ServicesModule());
        install(new MongoCoreModule());
        install(new MongoDaoModule());
        install(new MongoSearchModule());
        install(new ValidationModule());
        install(new GitSecurityModule());
        install(new GitServletModule());
        install(new RTFilesystemGitLoaderModule());
        install(new RTGitBootstrapModule());
        install(new FileSystemCodeServeModule());
    }
}
