package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.MatchDao;
import dev.getelements.elements.sdk.dao.Matchmaker;
import dev.getelements.elements.dao.mongo.model.match.MongoMatch;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NoSuitableMatchException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.match.Match;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.morphia.query.filters.Filters;
import org.bson.types.ObjectId;
import dev.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.fail;

@Test(enabled = false)
@Guice(modules = IntegrationTestModule.class)
public class MongoMatchmakerIntegrationTest {

    private static final String TEST_SCOPE = "test-scope";

    private static final String TEST_METADATA_KEY = "md_key";

    private static final String TEST_METADATA_VALUE = "md_value";

    private static final Logger logger = LoggerFactory.getLogger(MongoMatchmakerIntegrationTest.class);

    private MatchingMockObjects matchingMockObjects;

    private MatchDao matchDao;

    private Datastore Datastore;

    private List<Match> intermediateMatches = new ArrayList<>();

    private List<Profile> intermediateProfiles = new ArrayList<>();

    private List<Matchmaker.SuccessfulMatchTuple> intermediateSuccessfulMatchTuples = new ArrayList<>();

    @DataProvider
    public static Object[][] matchingAlgorithmsAndScopes() {
        return new Object[][] {
            new Object[] { null },
            new Object[] { TEST_SCOPE }
        };
    }

    @Test(dataProvider = "matchingAlgorithmsAndScopes")
    public void testMatch(final String scope) {

        logger.info("Testing default matching algoirthm.");

        final Application application = getMatchingMockObjects().makeMockApplication();

        final User usera = getMatchingMockObjects().makeMockUser();
        final User userb = getMatchingMockObjects().makeMockUser();

        final Profile profilea = getMatchingMockObjects().makeMockProfile(usera, application);
        final Profile profileb = getMatchingMockObjects().makeMockProfile(userb, application);

        final Match matcha = getMatchDao().createMatch(makeMockMatch(profilea, scope));

        try {
            getMatchDao().getDefaultMatchmaker()
                         .attemptToFindOpponent(matcha,  (p, o) -> "fail");
            fail("Matched when not matches were expected.");
        } catch (NoSuitableMatchException ex) {
            logger.info("Caught expected exception.");
        }

        final Match matchb = getMatchDao().createMatch(makeMockMatch(profileb, scope));

        final Matchmaker.SuccessfulMatchTuple successfulMatchTuple;
        successfulMatchTuple = getMatchDao()
            .getDefaultMatchmaker()
            .withScope(scope)
            .attemptToFindOpponent(matchb, (p, o) -> Stream.of(p.getId(), o.getId()).sorted().collect(joining("+")));

        // Cross validates that the matches were made properly
        crossValidateMatch(successfulMatchTuple, profileb, profilea);

        final MongoMatch mongoMatcha;
        mongoMatcha = getDatastore().find(MongoMatch.class)
                .filter(Filters.eq("_id", new ObjectId(successfulMatchTuple.getPlayerMatch().getId()))).first();

        final MongoMatch mongoMatchb;
        mongoMatchb = getDatastore().find(MongoMatch.class)
                .filter(Filters.eq("_id", new ObjectId(successfulMatchTuple.getOpponentMatch().getId()))).first();

        Assert.assertEquals(mongoMatcha.getScope(), scope);
        Assert.assertEquals(mongoMatchb.getScope(), scope);

        assertNotNull(mongoMatcha.getExpiry());
        assertNotNull(mongoMatchb.getExpiry());

        Assert.assertEquals(mongoMatcha.getGameId(), Stream.of(matcha.getId(), matchb.getId()).sorted().collect(joining("+")));
        Assert.assertEquals(mongoMatchb.getGameId(), Stream.of(matcha.getId(), matchb.getId()).sorted().collect(joining("+")));

        Assert.assertEquals(mongoMatcha.getMetadata().get(TEST_METADATA_KEY), TEST_METADATA_VALUE);
        Assert.assertEquals(mongoMatchb.getMetadata().get(TEST_METADATA_KEY), TEST_METADATA_VALUE);

        intermediateMatches.add(matcha);
        intermediateMatches.add(matchb);

        intermediateProfiles.add(profilea);
        intermediateProfiles.add(profileb);

        intermediateSuccessfulMatchTuples.add(successfulMatchTuple);

    }

    private Match makeMockMatch(final Profile profile, final String scope) {

        final Match match = new Match();
        match.setPlayer(profile);
        match.setScheme("pvp");
        match.setScope(scope);

        final Map<String, Object> metadata = new HashMap<>();
        metadata.put(TEST_METADATA_KEY, TEST_METADATA_VALUE);
        match.setMetadata(metadata);

        return match;

    }

    private void crossValidateMatch(final Matchmaker.SuccessfulMatchTuple successfulMatchTuple,
                                    final Profile player,
                                    final Profile opponent) {

        final Match playerMatch = successfulMatchTuple.getPlayerMatch();
        final Match opponentMatch = successfulMatchTuple.getOpponentMatch();

        assertEquals(playerMatch.getScope(), opponentMatch.getScope());
        assertEquals(playerMatch.getScheme(), opponentMatch.getScheme());

        assertEquals(playerMatch.getPlayer(), player);
        assertEquals(playerMatch.getOpponent(), opponent);

        assertEquals(opponentMatch.getPlayer(), opponent);
        assertEquals(opponentMatch.getOpponent(), player);

    }

    @DataProvider
    public Object[][] intermediateMatchDataProvider() {
        return intermediateMatches
            .stream()
            .map(m -> new Object[]{m})
            .collect(toList())
            .toArray(new Object[][]{});
    }

    @DataProvider
    public Object[][] intermediateProfileDataProvider() {
        return intermediateProfiles
            .stream()
            .map(m -> new Object[]{m})
            .collect(toList())
            .toArray(new Object[][]{});
    }


    @DataProvider
    public Object[][] intermediateSuccessfulMatchTupleProvider() {
        return intermediateSuccessfulMatchTuples
            .stream()
            .map(t -> new Object[]{t})
            .collect(toList())
            .toArray(new Object[][]{});
    }

    @Test(dataProvider = "intermediateMatchDataProvider", dependsOnMethods = "testMatch")
    public void attemptDeleteIntermediateMatchesAfterFinalization(final Match match) {
        try {
            getMatchDao().deleteMatch(match.getPlayer().getId(), match.getId());
            fail("Expected InvalidDataException here.");
        } catch (InvalidDataException ex) {
            // Test Passes
        }
    }

    public MatchingMockObjects getMatchingMockObjects() {
        return matchingMockObjects;
    }

    @Inject
    public void setMatchingMockObjects(MatchingMockObjects matchingMockObjects) {
        this.matchingMockObjects = matchingMockObjects;
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

}
