package com.namazustudios.socialengine.setup.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.setup.LineReaderSecureReader;
import com.namazustudios.socialengine.setup.SecureReader;
import com.namazustudios.socialengine.setup.commands.Root;
import com.namazustudios.socialengine.setup.provider.LineReaderProvider;
import com.namazustudios.socialengine.setup.provider.TerminalProvider;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.setup.SetupCommand.*;

public class SetupCommandModule extends PrivateModule {

    private InputStream stdin = System.in;

    private OutputStream stdout = System.out;

    private OutputStream stderr = System.err;

    @Override
    protected void configure() {

        requireBinding(SecureReader.class);

        bind(Root.class).asEagerSingleton();

        // Input streams
        bind(InputStream.class).annotatedWith(named(STDIN)).toInstance(stdin);

        // Output Streams
        bind(OutputStream.class).annotatedWith(named(STDOUT)).toInstance(stdout);
        bind(OutputStream.class).annotatedWith(named(STDERR)).toInstance(stderr);

        // Print Writers
        bind(PrintWriter.class).annotatedWith(named(STDOUT)).toInstance(new PrintWriter(stdout));
        bind(PrintWriter.class).annotatedWith(named(STDERR)).toInstance(new PrintWriter(stderr));

        bind(Terminal.class).toProvider(TerminalProvider.class).asEagerSingleton();
        bind(LineReader.class).toProvider(LineReaderProvider.class).asEagerSingleton();
        bind(SecureReader.class).to(LineReaderSecureReader.class).asEagerSingleton();

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
