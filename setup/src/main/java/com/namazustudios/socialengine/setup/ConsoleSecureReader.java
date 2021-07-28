package com.namazustudios.socialengine.setup;

import com.google.common.base.Strings;

import java.io.Console;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ConsoleSecureReader implements SecureReader {

    @Override
    public String read(final String fmt, final Object... args) {

        final var console = System.console();

        if (console == null) {
            throw new ConsoleException("No console instance available.  Please pass setup params via args.");
        }

        String value;

        do {
            value = console.readLine(fmt, args).trim();
        } while (isNullOrEmpty(value));

        return value;

    }

    @Override
    public String reads(final String fmt, final Object... args) {

        final var console = System.console();

        if (console == null) {
            throw new ConsoleException("No console instance available.  Please pass setup params via args.");
        }

        String value;

        do {
            value = new String(console.readPassword(fmt, args)).trim();
        } while (isNullOrEmpty(value));

        return value;

    }

}
