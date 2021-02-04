package com.namazustudios.socialengine.setup.commands;

import com.namazustudios.socialengine.setup.SetupCommand;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Created by patricktwohig on 7/31/17.
 */
public class DumpDefaultProperties implements SetupCommand {

    @Inject
    @Named(STDOUT)
    private PrintWriter stdout;

    @Override
    public void run(String[] args) throws Exception {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Properties defaultProperties = defaultConfigurationSupplier.get();
        defaultProperties.list(stdout);
        stdout.flush();

    }

}
