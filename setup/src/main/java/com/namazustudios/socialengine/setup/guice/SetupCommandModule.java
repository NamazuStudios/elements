package com.namazustudios.socialengine.setup.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import com.namazustudios.socialengine.service.BuildPropertiesVersionService;
import com.namazustudios.socialengine.service.Unscoped;
import com.namazustudios.socialengine.service.VersionService;
import com.namazustudios.socialengine.setup.SecureReader;
import com.namazustudios.socialengine.setup.Setup;
import com.namazustudios.socialengine.setup.commands.Root;
import ru.vyarus.guice.validator.ValidationModule;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.setup.SetupCommand.*;
import static org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY;

public class SetupCommandModule extends PrivateModule {

    private InputStream stdin = System.in;

    private OutputStream stdout = System.out;

    private OutputStream stderr = System.err;

    @Override
    protected void configure() {

        requireBinding(SecureReader.class);

        bind(Root.class).asEagerSingleton();
        bind(InputStream.class).annotatedWith(named(STDIN)).toInstance(stdin);
        bind(PrintWriter.class).annotatedWith(named(STDOUT)).toInstance(new PrintWriter(stdout));
        bind(PrintWriter.class).annotatedWith(named(STDERR)).toInstance(new PrintWriter(stderr));
        bind(VersionService.class).to(BuildPropertiesVersionService.class);
        bind(VersionService.class).annotatedWith(Unscoped.class).to(BuildPropertiesVersionService.class);

        expose(Root.class);

    }

    public SetupCommandModule withStdin(final InputStream stdin) {
        this.stdin = stdin;
        return this;
    }

    public SetupCommandModule withStdout(final OutputStream stdout) {
        this.stdout = stdout;
        return this;
    }

    public SetupCommandModule withStderr(final OutputStream stdout) {
        this.stdout = stdout;
        return this;
    }

}
