package com.namazustudios.socialengine.setup;

import com.google.inject.Guice;
import com.namazustudios.socialengine.setup.guice.SshServerModule;
import com.namazustudios.socialengine.setup.guice.SetupCommonModule;

import java.io.IOException;

public class SetupSSHDMain {

    private final SetupSSHD setup;

    public SetupSSHDMain(final String[] args) {

        final var injector = Guice.createInjector(
            new SshServerModule(),
            new SetupCommonModule()
        );

        setup = injector.getInstance(SetupSSHD.class);

    }

    public void start() throws IOException {
        setup.start();
    }

    public void stop() throws IOException {
        setup.stop();
    }

    public void run() throws IOException {
        setup.run();
    }

    public static void main(final String[] args) throws Exception {
        final var instance = new SetupSSHDMain(args);
        try {
            instance.run();
        } finally {
            instance.stop();
        }
    }

}
