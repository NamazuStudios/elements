package com.namazustudios.socialengine.setup;

import com.google.common.base.Strings;

import java.io.Console;

public class ConsoleSecureReader implements SecureReader {

    @Override
    public String reads(final String fmt, Object... args) {

        final Console console = System.console();

        if (console == null) {
            throw new ConsoleException("No console instance available.  Please pass setup params via args.");
        }

        String value;

        do {
            value = new String(console.readPassword(fmt, args)).trim();
        } while (Strings.isNullOrEmpty(value));

        return value;

    }

}
