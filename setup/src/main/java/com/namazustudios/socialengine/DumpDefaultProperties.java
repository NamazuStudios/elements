package com.namazustudios.socialengine;

import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;

import java.util.Properties;

/**
 * Created by patricktwohig on 7/31/17.
 */
public class DumpDefaultProperties implements Command {

    @Override
    public void run(String[] args) throws Exception {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final Properties defaultProperties = defaultConfigurationSupplier.get();
        defaultProperties.list(System.out);

    }

}
