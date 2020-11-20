package com.namazustudios.socialengine;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.config.FacebookBuiltinPermissionsSupplier;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.guice.FacebookBuiltinPermissionsModule;
import ru.vyarus.guice.validator.ValidationModule;

import java.io.Console;
import java.util.Arrays;
import java.util.Properties;

import static org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY;

/**
 * A Setup utility that can be run from the command-line.  This operates git-style where
 * there are several sub-commands to do things to get the application up and running.
 *
 * This class starts sets up the IoC container, parses out the first command passed, and
 * instantiates the individual {@link Command} which processes the rest from there.
 *
 */
public class Setup {

    /**
     * Runs the command.
     *
     * @param args the argument list.
     *
     * @throws Exception in case something goes wrong.
     */
    public void run(final String args[]) throws Exception {

        final Properties systemProperties = System.getProperties();

        if (!systemProperties.containsKey(DEFAULT_LOG_LEVEL_KEY)) {
            systemProperties.setProperty(DEFAULT_LOG_LEVEL_KEY, "warn");
        }

        if (args.length < 1) {
            System.err.print("Please specify commandType.");
        }

        final Class<? extends Command> commandType;

        if (args.length == 0) {
            System.out.printf("Missing command.  Supported commands are:\n");

            for (SupportedCommand supportedCommand : SupportedCommand.values()) {
                System.out.println("    " + supportedCommand.commandName);
            }

            return;
        }

        try {
            commandType = SupportedCommand.getCommandForName(args[0]);
        } catch (IllegalArgumentException ex) {

            System.out.printf("Unknown command %s.  Supported commands are:\n", args[0]);

            for (SupportedCommand supportedCommand : SupportedCommand.values()) {
                System.out.println("    " + supportedCommand.commandName);
            }

            return;
        }

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier(Setup.class.getClassLoader());

        final FacebookBuiltinPermissionsSupplier facebookBuiltinPermissionsSupplier;
        facebookBuiltinPermissionsSupplier = new FacebookBuiltinPermissionsSupplier();

        final Injector injector = Guice.createInjector(
                new ConfigurationModule(defaultConfigurationSupplier),
                new FacebookBuiltinPermissionsModule(facebookBuiltinPermissionsSupplier),
                new MongoCoreModule(),
                new MongoDaoModule(),
                new MongoSearchModule(),
                new ValidationModule(),
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
            throw new ConsoleException("No console instance available.  Please pass setup params via args.");
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
            throw new ConsoleException("No console instance available.  Please pass setup params via args.");
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

        ADD_USER("add-user", AddUser.class),
        UPDATE_USER("update-user", UpdateUser.class),
        DUMP_DEFAULT_PROPERTIES("dump-default-properties", DumpDefaultProperties.class);

        public final String commandName;
        public final Class<? extends Command> commandType;

        SupportedCommand(final String commandName, final Class<? extends  Command> command) {
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

    public static class ConsoleException extends IllegalStateException {
        public ConsoleException(String s) {
            super(s);
        }
    }

}
