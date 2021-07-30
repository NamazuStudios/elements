package com.namazustudios.socialengine.setup.provider;

import com.namazustudios.socialengine.rt.exception.InternalException;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.namazustudios.socialengine.setup.SetupCommand.STDIN;
import static com.namazustudios.socialengine.setup.SetupCommand.STDOUT;

public class TerminalProvider implements Provider<Terminal> {

    private Provider<InputStream> stdinProvider;

    private Provider<OutputStream> stdoutProvider;

    @Override
    public Terminal get() {

        final var attributes = new Attributes();

        try {
            return TerminalBuilder.builder()
                    .attributes(attributes)
                    .name("Elements Setup Terminal")
                    .system(false)
                    .streams(getStdinProvider().get(), getStdoutProvider().get())
                .build();
        } catch (IOException e) {
            throw new InternalException(e);
        }

    }

    public Provider<InputStream> getStdinProvider() {
        return stdinProvider;
    }

    @Inject
    public void setStdinProvider(@Named(STDIN) Provider<InputStream> stdinProvider) {
        this.stdinProvider = stdinProvider;
    }

    public Provider<OutputStream> getStdoutProvider() {
        return stdoutProvider;
    }

    @Inject
    public void setStdoutProvider(@Named(STDOUT) Provider<OutputStream> stdoutProvider) {
        this.stdoutProvider = stdoutProvider;
    }

}
