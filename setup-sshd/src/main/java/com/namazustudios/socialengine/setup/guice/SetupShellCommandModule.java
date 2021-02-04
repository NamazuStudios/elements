package com.namazustudios.socialengine.setup.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.setup.SecureReader;
import com.namazustudios.socialengine.setup.commands.SetupShell;

public class SetupShellCommandModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(SetupShell.class).asEagerSingleton();
        bind(SecureReader.class).to(SetupShell.class);

        expose(SetupShell.class);
        expose(SecureReader.class);

    }

}
