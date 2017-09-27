package com.namazustudios.socialengine.dao.mongo;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.apache.bval.guice.ValidationModule;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_DB_URLS;
import static de.flapdoodle.embed.mongo.MongodStarter.getDefaultInstance;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static java.lang.String.format;

@Test
@Guice(modules = MongoFIFOMatchmakerIntegrationTest.Module.class)
public class MongoFIFOMatchmakerIntegrationTest {

    private static final int TEST_MONGO_PORT = 45000;

    private static final String TEST_BIND_IP = "localhost";

    private MatchDao matchDao;

    private MongodProcess mongodProcess;

    private MongodExecutable mongodExecutable;

    @BeforeTest
    public void insertMatches() {
        System.out.println("Hello World!");
    }

    @Test
    public void testMatch() {
        System.out.println("Hello World!");
    }

    @AfterClass
    public void killProcess() {
        getMongodProcess().stop();
        getMongodExecutable().stop();
    }

    public MatchDao getMatchDao() {
        return matchDao;
    }

    @Inject
    public void setMatchDao(MatchDao matchDao) {
        this.matchDao = matchDao;
    }

    public MongodProcess getMongodProcess() {
        return mongodProcess;
    }

    @Inject
    public void setMongodProcess(MongodProcess mongodProcess) {
        this.mongodProcess = mongodProcess;
    }

    public MongodExecutable getMongodExecutable() {
        return mongodExecutable;
    }

    @Inject
    public void setMongodExecutable(MongodExecutable mongodExecutable) {
        this.mongodExecutable = mongodExecutable;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            try {
                final MongodExecutable executable = mongodExecutable();
                bind(MongodExecutable.class).toInstance(executable);
                bind(MongodProcess.class).toInstance(executable.start());
            } catch (IOException e) {
                addError(e);
                return;
            }

            final DefaultConfigurationSupplier defaultConfigurationSupplier;
            defaultConfigurationSupplier = new DefaultConfigurationSupplier();

            install(new ConfigurationModule(() -> {
                final Properties properties = defaultConfigurationSupplier.get();
                properties.put(MONGO_DB_URLS, format("mongo://%s:%d", TEST_BIND_IP, TEST_MONGO_PORT));
                return properties;
            }));

            install(new MongoDaoModule());
            install(new MongoCoreModule());
            install(new MongoSearchModule());
            install(new ValidationModule());

        }

        public MongodExecutable mongodExecutable() throws IOException {

            final IMongodConfig config = new MongodConfigBuilder()
                .version(Version.V3_4_1)
                .net(new Net(TEST_BIND_IP, TEST_MONGO_PORT, localhostIsIPv6()))
                .build();

            final MongodStarter starter = getDefaultInstance();
            return starter.prepare(config);

        }

    }

}
