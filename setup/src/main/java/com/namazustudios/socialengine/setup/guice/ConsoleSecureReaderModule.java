package com.namazustudios.socialengine.setup.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.setup.ConsoleSecureReader;
import com.namazustudios.socialengine.setup.SecureReader;

public class ConsoleSecureReaderModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SecureReader.class).to(ConsoleSecureReader.class);
    }
}
