package com.namazustudios.socialengine.codeserve;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFilesystemGitLoaderModule;
import com.namazustudios.socialengine.dao.rt.guice.RTGitBootstrapModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import org.apache.bval.guice.ValidationModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

public class CodeServeModule extends AbstractModule {

    private final List<Module> modules = new ArrayList<>();

    private final Supplier<Properties> configurationSupplier;

    public CodeServeModule(final Supplier<Properties> configurationSupplier) {
        this.configurationSupplier = configurationSupplier;
    }

    public CodeServeModule withModule(Module m){
        modules.add(m);
        return this;
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
        install(new RTFilesystemGitLoaderModule());
        install(new FileSystemCodeServeModule());
        for(Module m : modules){
            install(m);
        }
    }

}
