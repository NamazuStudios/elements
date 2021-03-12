package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.profile.Profile;
import dev.morphia.Datastore;
import org.testng.annotations.*;

import javax.inject.Inject;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(enabled = false)
@Guice(modules = IntegrationTestModule.class)
public class MongoMatchDaoCreateDeleteIntegrationTest {

    private MatchDao matchDao;

    private EmbeddedMongo embeddedMongo;

    private Datastore Datastore;

    private MatchingMockObjects matchingMockObjects;

    @Test(expectedExceptions = NotFoundException.class)
    public void performTest() {

        final User user = getMatchingMockObjects().makeMockUser("test-user");
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

    @BeforeClass
    public void dropDatabase() {
        getEmbeddedMongo().getMongoDatabase().drop();
    }

    @AfterSuite
    public void killProcess() {
        getEmbeddedMongo().stop();
    }

    public MatchDao getMatchDao() {
        return matchDao;
    }

    @Inject
    public void setMatchDao(MatchDao matchDao) {
        this.matchDao = matchDao;
    }

    public EmbeddedMongo getEmbeddedMongo() {
        return embeddedMongo;
    }

    @Inject
    public void setEmbeddedMongo(EmbeddedMongo embeddedMongo) {
        this.embeddedMongo = embeddedMongo;
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
