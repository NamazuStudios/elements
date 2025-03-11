package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.MatchDao;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.match.Match;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.morphia.Datastore;
import org.testng.annotations.*;

import jakarta.inject.Inject;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(enabled = false)
@Guice(modules = IntegrationTestModule.class)
public class MongoMatchDaoCreateDeleteIntegrationTest {

    private MatchDao matchDao;

    private Datastore Datastore;

    private MatchingMockObjects matchingMockObjects;

    @Test(expectedExceptions = NotFoundException.class)
    public void performTest() {

        final User user = getMatchingMockObjects().makeMockUser();
        final Application application = getMatchingMockObjects().makeMockApplication();

        final Profile profile = getMatchingMockObjects().makeMockProfile(user, application);
        final Match match = getMatchDao().createMatch(makeMockMatch(profile));

        getMatchDao().deleteMatch(match.getPlayer().getId(), match.getId());
        getMatchDao().getMatchForPlayer(match.getPlayer().getId(), match.getId());

    }

    private Match makeMockMatch(final Profile profile) {
        final Match match = new Match();
        match.setPlayer(profile);
        match.setScheme("pvp");
        return match;
    }

    public MatchDao getMatchDao() {
        return matchDao;
    }

    @Inject
    public void setMatchDao(MatchDao matchDao) {
        this.matchDao = matchDao;
    }

    public Datastore getDatastore() {
        return Datastore;
    }

    @Inject
    public void setDatastore(Datastore Datastore) {
        this.Datastore = Datastore;
    }

    public MatchingMockObjects getMatchingMockObjects() {
        return matchingMockObjects;
    }

    @Inject
    public void setMatchingMockObjects(MatchingMockObjects matchingMockObjects) {
        this.matchingMockObjects = matchingMockObjects;
    }

}
