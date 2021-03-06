package com.namazustudios.socialengine.setup.jline;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.List;

import static com.namazustudios.socialengine.setup.SetupCommands.streamShellCommands;
import static java.util.stream.Collectors.toList;

public class ShellCompleter implements Completer {

    private final Completer delegate;

    public ShellCompleter() {

        final var strings = streamShellCommands()
            .map(c -> c.commandName)
            .sorted()
            .collect(toList());

        delegate = new StringsCompleter(strings);

    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        delegate.complete(reader, line, candidates);
    }

}
