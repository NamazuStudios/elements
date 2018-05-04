package com.namazustudios.socialengine.dao.mongo;

import com.google.common.collect.Streams;
import com.namazustudios.socialengine.dao.MatchDao;
import com.namazustudios.socialengine.dao.Matchmaker;
import com.namazustudios.socialengine.dao.mongo.model.MongoMatch;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NoSuitableMatchException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import com.namazustudios.socialengine.model.profile.Profile;
import org.bson.types.ObjectId;
import org.mongodb.morphia.AdvancedDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.namazustudios.socialengine.model.match.MatchingAlgorithm.FIFO;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.fail;

@Guice(modules = IntegrationTestModule.class)
public class MongoMatchmakerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoMatchmakerIntegrationTest.class);

    private EmbeddedMongo embeddedMongo;

    private MatchingMockObjects matchingMockObjects;

    private MatchDao matchDao;

    private AdvancedDatastore advancedDatastore;

    private List<Match> intermediateMatches = new ArrayList<>();

    private List<Profile> intermediateProfiles = new ArrayList<>();

    private List<Matchmaker.SuccessfulMatchTuple> intermediateSuccessfulMatchTuples = new ArrayList<>();

    @DataProvider
    public static Iterator<Object[]> matchingAlgorithms() {
        return asList(MatchingAlgorithm.values())
            .stream()
            .map(algo -> new Object[]{algo})
            .collect(toList())
            .iterator();
    }

    @Test(dataProvider = "matchingAlgorithms")
    public void testMatch(final MatchingAlgorithm matchingAlgorithm) {

        logger.info("Testing matching algorithm {}", matchingAlgorithm);

        final Application application = getMatchingMockObjects().makeMockApplication();

        final User usera = getMatchingMockObjects().makeMockUser("test-user-a");
        final User userb = getMatchingMockObjects().makeMockUser("test-user-b");

        final Profile profilea = getMatchingMockObjects().makeMockProfile(usera, application);
        final Profile profileb = getMatchingMockObjects().makeMockProfile(userb, application);

        final Match matcha = getMatchDao().createMatch(makeMockMatch(profilea));

        try {
            getMatchDao().getMatchmaker(matchingAlgorithm)
                         .attemptToFindOpponent(matcha,  (p, o) -> "fail");
            fail("Matched when not matches were expected.");
        } catch (NoSuitableMatchException ex) {
            logger.info("Caught expected exception.");
        }

        final Match matchb = getMatchDao().createMatch(makeMockMatch(profileb));

        final Matchmaker.SuccessfulMatchTuple successfulMatchTuple;
        successfulMatchTuple = getMatchDao()
            .getMatchmaker(FIFO)
            .attemptToFindOpponent(matchb, (p, o) -> Stream.of(p.getId(), o.getId()).sorted().collect(joining("+")));

        // Cross validates that the matches were made properly
        crossValidateMatch(successfulMatchTuple, profileb, profilea);

        final MongoMatch mongoMatcha;
        mongoMatcha = getAdvancedDatastore().get(MongoMatch.class, new ObjectId(successfulMatchTuple.getPlayerMatch().getId()));

        final MongoMatch mongoMatchb;
        mongoMatchb = getAdvancedDatastore().get(MongoMatch.class, new ObjectId(successfulMatchTuple.getOpponentMatch().getId()));

        assertNotNull(mongoMatcha.getExpiry());
        assertNotNull(mongoMatchb.getExpiry());

        assertEquals(mongoMatcha.getGameId(), Stream.of(matcha.getId(), matchb.getId()).sorted().collect(joining("+")));
        assertEquals(mongoMatchb.getGameId(), Stream.of(matcha.getId(), matchb.getId()).sorted().collect(joining("+")));

        intermediateMatches.add(matcha);
        intermediateMatches.add(matchb);

        intermediateProfiles.add(profilea);
        intermediateProfiles.add(profileb);

        intermediateSuccessfulMatchTuples.add(successfulMatchTuple);

    }

    private Match makeMockMatch(final Profile profile) {
        final Match match = new Match();
        match.setPlayer(profile);
        match.setScheme("pvp");
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

    @BeforeClass
    public void dropDatabase() {
        getEmbeddedMongo().getMongoDatabase().drop();
    }

    @AfterSuite
    public void killProcess() {
        getEmbeddedMongo().stop();
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

    public AdvancedDatastore getAdvancedDatastore() {
        return advancedDatastore;
    }

    @Inject
    public void setAdvancedDatastore(AdvancedDatastore advancedDatastore) {
        this.advancedDatastore = advancedDatastore;
    }

    public EmbeddedMongo getEmbeddedMongo() {
        return embeddedMongo;
    }

    @Inject
    public void setEmbeddedMongo(EmbeddedMongo embeddedMongo) {
        this.embeddedMongo = embeddedMongo;
    }

}

