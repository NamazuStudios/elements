package com.namazustudios.socialengine.dao.mongo;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.dao.mongo.guice.MongoCoreModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.socialengine.dao.mongo.guice.MongoSearchModule;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import org.apache.bval.guice.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

import static com.namazustudios.socialengine.dao.mongo.provider.MongoClientProvider.MONGO_DB_URLS;
import static com.namazustudios.socialengine.model.User.Level.USER;
import static com.namazustudios.socialengine.model.match.MatchingAlgorithm.FIFO;
import static de.flapdoodle.embed.mongo.MongodStarter.getDefaultInstance;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.fail;

@Test
@Guice(modules = MongoFIFOMatchmakerIntegrationTest.Module.class)
public class MongoFIFOMatchmakerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoFIFOMatchmakerIntegrationTest.class);

    private static final int TEST_MONGO_PORT = 45000;

    private static final String TEST_BIND_IP = "localhost";

    private ApplicationDao applicationDao;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    private UserDao userDao;

    private ProfileDao profileDao;

    private MatchDao matchDao;

    private MongodProcess mongodProcess;

    private MongodExecutable mongodExecutable;

    @Test
    public void testMatch() {

        final Application application = getApplicationDao().createOrUpdateInactiveApplication(makeMockApplication());

        final User usera = getUserDao().createOrReactivateUser(makeMockUser("test-user-a"));
        final User userb = getUserDao().createOrReactivateUser(makeMockUser("test-user-b"));

        final Profile profilea = getProfileDao().createOrReactivateProfile(makeMockProfile(usera, application));
        final Profile profileb = getProfileDao().createOrReactivateProfile(makeMockProfile(userb, application));

        final Match matcha = getMatchDao().createMatchAndLogDelta(makeMockMatch(profilea)).getMatch();

        try {
            getMatchDao().getMatchmaker(FIFO).attemptToFindOpponent(matcha);
            fail("Matched when not matches were expected.");
        } catch (NoSuitableMatchException ex) {
            logger.info("Caught expected exception.");
        }

        final Match matchb = getMatchDao().createMatchAndLogDelta(makeMockMatch(profileb)).getMatch();

        final Matchmaker.SuccessfulMatchTuple successfulMatchTuple;
        successfulMatchTuple = getMatchDao().getMatchmaker(FIFO).attemptToFindOpponent(matchb);

        // Cross validates that the matches were made properly
        crossValidateMatch(successfulMatchTuple, profileb, profilea);

    }

    private Application makeMockApplication() {
        final Application application = new Application();
        application.setName("mock");
        application.setDescription("A mock application.");
        return application;
    }

    private User makeMockUser(final String name) {
        final User user = new User();
        user.setName(name);
        user.setEmail(format("%s@example.com", name));
        user.setLevel(USER);
        return user;
    }

    private Profile makeMockProfile(final User user, final Application application) {
        final Profile profile =  new Profile();
        profile.setUser(user);
        profile.setApplication(application);
        profile.setDisplayName(format("display-name-%s", user.getName()));
        profile.setImageUrl(format("http://example.com/%s.png", user.getName()));
        return profile;
    }

    private Match makeMockMatch(final Profile profile) {
        final Match match = new Match();
        match.setPlayer(profile);
        return match;
    }

    private void crossValidateMatch(final Matchmaker.SuccessfulMatchTuple successfulMatchTuple,
                                    final Profile player,
                                    final Profile opponent) {

        final Match playerMatch = successfulMatchTuple.getPlayerMatch();
        final Match opponentMatch = successfulMatchTuple.getOpponentMatch();

        assertEquals(playerMatch.getPlayer(), player);
        assertEquals(playerMatch.getOpponent(), opponent);

        assertEquals(opponentMatch.getPlayer(), opponent);
        assertEquals(opponentMatch.getOpponent(), player);

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

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
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
