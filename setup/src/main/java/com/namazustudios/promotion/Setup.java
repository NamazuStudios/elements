package com.namazustudios.promotion;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.promotion.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.promotion.rest.guice.ConfigurationModule;

import java.io.Console;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class Setup {


    public void run(final String args[]) throws Exception {

        if (args.length < 1) {
            System.err.print("Please specify commandType.");
        }

        final Class<? extends Command> commandType;

        try {
            commandType = SupportedCommand.getCommandForName(args[0]);
        } catch (IllegalArgumentException ex) {
            System.out.printf("Unknown commandType %s.  Supported commands are:\n", args[0]);

            for (SupportedCommand supportedCommand : SupportedCommand.values()) {
                System.out.println("    " + supportedCommand.commandName);
            }

            return;
        }

        final Injector injector = Guice.createInjector(
                new ConfigurationModule(),
                new MongoDaoModule(),
                new AbstractModule() {

                    @Override
                    protected void configure() {
                        binder().bind(Setup.class).toInstance(Setup.this);
                    }

                });

        final Command command = injector.getInstance(commandType);
        final String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        command.run(subArgs);

    }

    public String read(final String fmt, Object... args) {

        final Console console = System.console();

        if (console == null) {
            throw new IllegalStateException("No console instance available.  Please pass setup params via args.");
        }

        String value;

        do {
            value = console.readLine(fmt, args).trim();
        } while (Strings.isNullOrEmpty(value));

        return value;

    }

    public String reads(final String fmt, Object... args) {

        final Console console = System.console();

        if (console == null) {
            throw new IllegalStateException("No console instance available.  Please pass setup params via args.");
        }

        String value;

        do {
            value = new String(console.readPassword(fmt, args)).trim();
        } while (Strings.isNullOrEmpty(value));

        return value;

    }

    public static void main( String[] args ) throws Exception {
        final Setup setup = new Setup();
        setup.run(args);
    }

    private enum SupportedCommand {

        ADD_USER("add-user", AddUser.class);

        public final String commandName;
        public final Class<? extends Command> commandType;

        private SupportedCommand(final String commandName, final Class<? extends  Command> command) {
            this.commandName = commandName;
            this.commandType = command;
        }

        public static Class<? extends Command> getCommandForName(final String name) {

            for (SupportedCommand supportedCommand : values()) {
                if (supportedCommand.commandName.equals(name)) {
                    return supportedCommand.commandType;
                }
            }

            throw new IllegalArgumentException("Unknown command: " + name);
        }

    }

}

