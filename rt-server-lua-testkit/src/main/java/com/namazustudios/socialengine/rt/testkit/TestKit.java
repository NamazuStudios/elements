package com.namazustudios.socialengine.rt.testkit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.testkit.TestRunner.LOGGER;
import static com.namazustudios.socialengine.rt.testkit.TestSuites.parseTestFiles;
import static java.lang.System.getProperties;
import static org.slf4j.event.Level.ERROR;
import static org.slf4j.impl.SimpleLogger.*;

/**
 * Main entry point.
 *
 */
public class TestKit {

    private static final String TEST_LOGGER_KEY = SYSTEM_PREFIX + LOGGER;

    private final String[] args;

    final OptionParser optionParser = new OptionParser();

    private final OptionSpec<String> projectPath = optionParser
            .accepts("project-root", "The root directory of all Lua scripts.")
            .withRequiredArg()
            .ofType(String.class)
            .defaultsTo(new File(".").getAbsolutePath());

    private final OptionSpec<String> test = optionParser
            .accepts("test", "The test (or tests).  Specified in the format of <module name>:<method name>")
            .withOptionalArg()
            .ofType(String.class)
            .withValuesSeparatedBy(",");

    private final OptionSpec<String> testFile = optionParser
            .accepts("test-suite", "A file (or files) which specifies a suite of tests.  Line by line.")
            .withOptionalArg()
            .ofType(String.class)
            .withValuesSeparatedBy(",");

    private final OptionSpec<String> logLevel = optionParser
            .accepts("log-level", "The default log level.")
            .withOptionalArg()
            .ofType(String.class)
            .defaultsTo(Level.ERROR.toString());

    private final List<Module> moduleList = new ArrayList<>();

    /**
     * Creates a new {@link TestKit} with the supplied arguments (presumably from the command line).
     *
     * @param args the arguments
     */
    public TestKit(final String[] args) {
        this.args = args.clone();
    }

    /**
     * Adds an addition {@link Module} to this {@link TestKit} instance.
     *
     * @param module the {@link Module} to add
     * @return this instance
     */
    public TestKit addModule(final Module module) {
        moduleList.add(module);
        return this;
    }

    /**
     * Returns the {@link OptionParser} used to parse the options of this runner.
     *
     * @return this instance's {@link OptionParser}
     */
    public OptionParser getOptionParser() {
        return optionParser;
    }

    /**
     * Invokes {@link #run(Consumer)} without any additional options.
     *
     * @throws Exception
     */
    public void run() throws Exception {
        run(optionSet -> {});
    }

    /**
     * Runs all of the tests, allowing for additional processing of extra options parsed from the command line.
     *
     * @param optionSetConsumer allows for the processing of any additional options
     * @throws Exception if there is any error running the tests.
     */
    public void run(final Consumer<OptionSet> optionSetConsumer) throws Exception {

        final Properties systemProperties = getProperties();

        if (!systemProperties.containsKey(LOG_FILE_KEY)) {
            systemProperties.setProperty(LOG_FILE_KEY, "System.out");
        }

        if (!systemProperties.containsKey(TEST_LOGGER_KEY)) {
            systemProperties.setProperty(TEST_LOGGER_KEY, ERROR.toString().toLowerCase());
        }

        final Injector injector;

        try {

            final OptionSet optionSet = optionParser.parse(args);

            if (!systemProperties.containsKey(DEFAULT_LOG_LEVEL_KEY)) {
                final String selectedLogLevel = logLevel.value(optionSet).toLowerCase();
                systemProperties.setProperty(DEFAULT_LOG_LEVEL_KEY, selectedLogLevel);
            }

            final Logger logger = LoggerFactory.getLogger(TestKit.class);

            final TestRunnerModule testRunnerModule = new TestRunnerModule()
                .addTests(test.values(optionSet))
                .addTests(parseTestFiles(testFile.values(optionSet)))
                .withProjectRoot(projectPath.value(optionSet));

            if (testRunnerModule.testCount() == 0) {
                logger.error("No tests defined.");
                optionParser.printHelpOn(System.out);
                return;
            }

            optionSetConsumer.accept(optionSet);

            final List<Module> moduleList = new ArrayList<>();
            moduleList.add(testRunnerModule);
            moduleList.addAll(this.moduleList);
            injector = Guice.createInjector(moduleList.toArray(new Module[]{}));

        } catch (OptionException ex) {
            optionParser.printHelpOn(System.out);
            return;
        } catch (UncheckedIOException ex) {
            final Logger logger = LoggerFactory.getLogger(TestKit.class);
            logger.error("Caught IO Exception reading test configuration.", ex);
            throw ex;
        }

        final TestRunner testRunner = injector.getInstance(TestRunner.class);
        testRunner.perform();

    }

}
