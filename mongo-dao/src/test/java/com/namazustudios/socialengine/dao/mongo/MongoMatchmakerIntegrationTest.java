package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import com.namazustudios.socialengine.model.profile.Profile;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.User.Level.USER;
import static com.namazustudios.socialengine.model.match.MatchingAlgorithm.FIFO;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.fail;

@Guice(modules = IntegrationTestModule.class)
public class MongoMatchmakerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoMatchmakerIntegrationTest.class);

    private ApplicationDao applicationDao;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    private UserDao userDao;

    private ProfileDao profileDao;

    private MatchDao matchDao;

    private MongodProcess mongodProcess;

    private MongodExecutable mongodExecutable;

    @DataProvider
    public static Iterator<Object[]> matchingAlgorithms() {
        return asList(MatchingAlgorithm.values())
            .stream()
            .map(algo -> new Object[]{algo})
            .collect(Collectors.toList())
            .iterator();
    }

    @Test(dataProvider = "matchingAlgorithms")
    public void testMatch(final MatchingAlgorithm matchingAlgorithm) {

        logger.info("Testing matching algorithm {}", matchingAlgorithm);

        final Application application = getApplicationDao().createOrUpdateInactiveApplication(makeMockApplication());

        final User usera = getUserDao().createOrReactivateUser(makeMockUser("test-user-a"));
        final User userb = getUserDao().createOrReactivateUser(makeMockUser("test-user-b"));

        final Profile profilea = getProfileDao().createOrReactivateProfile(makeMockProfile(usera, application));
        final Profile profileb = getProfileDao().createOrReactivateProfile(makeMockProfile(userb, application));

        final Match matcha = getMatchDao().createMatchAndLogDelta(makeMockMatch(profilea)).getMatch();

        try {
            getMatchDao().getMatchmaker(matchingAlgorithm).attemptToFindOpponent(matcha);
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

}
