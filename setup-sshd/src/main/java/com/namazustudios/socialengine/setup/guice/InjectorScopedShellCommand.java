package com.namazustudios.socialengine.setup.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.namazustudios.socialengine.setup.commands.SetupShell;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InjectorScopedShellCommand implements Command {

    private Injector injector;

    private InputStream stdin;

    private OutputStream stdout;

    private OutputStream stderr;

    private SetupShell shell;

    private ExitCallback exitCallback;

    @Override
    public void setInputStream(final InputStream in) {
        this.stdin = in;
    }

    @Override
    public void setOutputStream(final OutputStream out) {
        this.stdout = out;
    }

    @Override
    public void setErrorStream(final OutputStream err) {
        this.stderr = err;
    }

    @Override
    public void setExitCallback(final ExitCallback callback) {
        this.exitCallback = callback;
    }

    @Override
    public void start(final ChannelSession channel, final Environment env) throws IOException {

        final var injector = getInjector().createChildInjector(
            new SetupShellCommandModule(),
            new SetupCommandModule()
                .withStdin(this.stdin)
                .withStdout(this.stdout)
                .withStderr(this.stderr)
        );

        shell = injector.getInstance(SetupShell.class);

        shell.start().handleAsync((exitValue, throwable) -> {

            if (throwable == null) {
                exitCallback.onExit(exitValue);
            } else {
                exitCallback.onExit(exitValue, throwable.getMessage());
            }

            return null;

        });

    }

    @Override
    public void destroy(final ChannelSession channel) throws Exception {
        shell.close();
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

}
