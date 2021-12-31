package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.SaveDataDocumentDao;
import com.namazustudios.socialengine.dao.mongo.model.savedata.MongoSaveDataDocumentId;
import com.namazustudios.socialengine.exception.savedata.SaveDataNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;
import com.namazustudios.socialengine.model.user.User;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.dao.mongo.IntegrationTestModule.TEST_COMPONENT;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoSaveDataDocumentDaoTest {

    private Mapper mapper;

    private UserTestFactory userTestFactory;

    private ProfileTestFactory profileTestFactory;

    private ApplicationTestFactory applicationTestFactory;

    private Application application;

    private User testUserBob;

    private User testUserAlice;

    private User testUserTrudy;

    private Profile testProfileBob;

    private Profile testProfileAlice;

    private Profile testProfileTrudy;

    private SaveDataDocumentDao saveDataDocumentDao;

    private final Map<DocumentKey, SaveDataDocument> intermediateDocuments = new ConcurrentHashMap<>();

    @BeforeClass
    public void createTestUser() {

        application = getApplicationTestFactory().createMockApplication(MongoSaveDataDocumentDaoTest.class);

        testUserBob = getUserTestFactory().createTestUser();
        testProfileBob = getProfileTestFactory().makeMockProfile(testUserBob, application);

        testUserAlice = getUserTestFactory().createTestUser();
        testProfileAlice = getProfileTestFactory().makeMockProfile(testUserAlice, application);

        testUserTrudy = getUserTestFactory().createTestUser();
        testProfileTrudy = getProfileTestFactory().makeMockProfile(testUserTrudy, application);

    }

    @DataProvider
    public Object[][] getTestUsers() {
        return new Object[][] {
            {testUserBob},
            {testUserAlice}
        };
    }

    @DataProvider
    public Object[][] getTestProfiles() {
        return new Object[][] {
            {testProfileBob},
            {testProfileAlice}
        };
    }

    @DataProvider
    public Object[][] getTestUsersAndSlots() {
        return Stream.concat(
            range(0, 10).mapToObj(slot -> new Object[]{testUserBob, slot}),
            range(0, 10).mapToObj(slot -> new Object[]{testUserAlice, slot})
        ).toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getTestProfilesAndSlots() {
        return Stream.concat(
            range(0, 10).mapToObj(slot -> new Object[]{testProfileBob, slot}),
            range(0, 10).mapToObj(slot -> new Object[]{testProfileAlice, slot})
        ).toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getIntermediateSaveGameData() {
        return intermediateDocuments
            .values()
            .stream()
            .map(sdd -> new Object[]{sdd})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getTestUsersAndSlots")
    public void testCreateSaveDocumentUser(final User user, final int slot) {

        final var now = currentTimeMillis();
        final var doc = new SaveDataDocument();
        final var contents = testContentsFor(user, "initial " + slot);

        doc.setSlot(slot);
        doc.setUser(user);
        doc.setContents(contents);

        final var result = getSaveDataDocumentDao().createSaveDataDocument(doc);
        assertNotNull(result.getId());
        assertNotNull(result.getVersion());
        assertEquals(user, result.getUser());
        assertEquals(slot, result.getSlot());
        assertNull(result.getProfile());
        assertTrue(result.getTimestamp() >= now);

        putIntermediateDocument(user.getId(), slot, result);

    }

    @Test(dataProvider = "getTestProfilesAndSlots")
    public void testCreateSaveDocumentProfile(final Profile profile, final int slot) {

        final var now = currentTimeMillis();
        final var doc = new SaveDataDocument();
        final var contents = testContentsFor(profile, "initial " + slot);

        doc.setSlot(slot);
        doc.setProfile(profile);
        doc.setContents(contents);

        final var result = getSaveDataDocumentDao().createSaveDataDocument(doc);
        assertNotNull(result.getId());
        assertNotNull(result.getVersion());
        assertEquals(profile, result.getProfile());
        assertEquals(slot, result.getSlot());
        assertEquals(profile, result.getProfile());
        assertTrue(result.getTimestamp() >= now);

        putIntermediateDocument(profile.getId(), slot, result);

    }

    @Test(dataProvider = "getTestUsersAndSlots", dependsOnMethods = "testCreateSaveDocumentUser")
    public void testCheckedUpdateSaveDocumentUser(final User user, final int slot) {

        final var now = currentTimeMillis();
        final var doc = getIntermediateDocument(user.getId(), slot);
        final var contents = testContentsFor(user, "update " + slot);

        final var version = doc.getVersion();
        assertNotNull(version);

        assertEquals(slot, doc.getSlot());
        doc.setContents(contents);

        final var result = getSaveDataDocumentDao().checkedUpdate(doc);
        assertNotNull(result.getId());
        assertNotNull(result.getVersion());
        assertEquals(user, result.getUser());
        assertEquals(slot, result.getSlot());
        assertNotEquals(version, result.getVersion());
        assertNull(result.getProfile());
        assertTrue(result.getTimestamp() >= now);

        putIntermediateDocument(user.getId(), slot, result);

    }

    @Test(dataProvider = "getTestProfilesAndSlots", dependsOnMethods = "testCreateSaveDocumentProfile")
    public void testCheckedUpdateSaveDocumentProfile(final Profile profile, final int slot) {

        final var now = currentTimeMillis();
        final var doc = getIntermediateDocument(profile.getId(), slot);
        final var contents = testContentsFor(profile, "update " + slot);

        final var version = doc.getVersion();
        assertNotNull(version);

        assertEquals(slot, doc.getSlot());
        doc.setContents(contents);

        final var result = getSaveDataDocumentDao().checkedUpdate(doc);
        assertNotNull(result.getId());
        assertNotNull(result.getVersion());
        assertEquals(profile, result.getProfile());
        assertEquals(slot, result.getSlot());
        assertEquals(profile, result.getProfile());
        assertNotEquals(version, result.getVersion());
        assertTrue(result.getTimestamp() >= now);

        putIntermediateDocument(profile.getId(), slot, result);

    }

    @Test(
        dataProvider = "getTestUsersAndSlots",
        dependsOnMethods = "testCreateSaveDocumentProfile",
        expectedExceptions = SaveDataNotFoundException.class
    )
    public void testCheckedUpdateSaveDocumentUserFail(final User user, final int slot) {

        final var now = currentTimeMillis();
        final var doc = getIntermediateDocument(user.getId(), slot);
        final var contents = testContentsFor(user, "update " + slot);

        final var version = doc.getVersion();
        assertNotNull(version);

        assertEquals(slot, doc.getSlot());
        doc.setVersion("f00fab");
        doc.setContents(contents);

        final var result = getSaveDataDocumentDao().checkedUpdate(doc);
        assertNotNull(result.getId());
        assertNotNull(result.getVersion());
        assertEquals(user, result.getUser());
        assertEquals(slot, result.getSlot());
        assertNotEquals(version, result.getVersion());
        assertNull(result.getProfile());
        assertTrue(result.getTimestamp() >= now);

        putIntermediateDocument(user.getId(), slot, result);

    }

    @Test(
        dataProvider = "getTestProfilesAndSlots",
        dependsOnMethods = "testCreateSaveDocumentProfile",
        expectedExceptions = SaveDataNotFoundException.class
    )
    public void testCheckedUpdateSaveDocumentProfileFail(final Profile profile, final int slot) {

        final var now = currentTimeMillis();
        final var doc = getIntermediateDocument(profile.getId(), slot);
        final var contents = testContentsFor(profile, "update " + slot);

        final var version = doc.getVersion();
        assertNotNull(version);

        assertEquals(slot, doc.getSlot());
        doc.setVersion("f00fab");
        doc.setContents(contents);

        final var result = getSaveDataDocumentDao().checkedUpdate(doc);
        assertNotNull(result.getId());
        assertNotNull(result.getVersion());
        assertEquals(profile, result.getProfile());
        assertEquals(slot, result.getSlot());
        assertEquals(profile, result.getProfile());
        assertNotEquals(version, result.getVersion());
        assertTrue(result.getTimestamp() >= now);

        putIntermediateDocument(profile.getId(), slot, result);

    }

    @Test(dataProvider = "getTestUsersAndSlots", dependsOnMethods = "testCheckedUpdateSaveDocumentProfile")
    public void testForceUpdateSaveDocumentUser(final User user, final int slot) {

        final var now = currentTimeMillis();
        final var doc = getIntermediateDocument(user.getId(), slot);
        final var contents = testContentsFor(user, "forced update " + slot);

        doc.setVersion("f00f");
        doc.setContents(contents);
        assertEquals(slot, doc.getSlot());

        final var result = getSaveDataDocumentDao().forceUpdateSaveDataDocument(doc);
        assertNotNull(result.getId());
        assertNotNull(result.getVersion());
        assertEquals(user, result.getUser());
        assertEquals(slot, result.getSlot());
        assertNull(result.getProfile());
        assertTrue(result.getTimestamp() >= now);

        putIntermediateDocument(user.getId(), slot, result);

    }

    @Test(dataProvider = "getTestProfilesAndSlots", dependsOnMethods = "testCheckedUpdateSaveDocumentProfile")
    public void testForceUpdateSaveDocumentProfile(final Profile profile, final int slot) {

        final var now = currentTimeMillis();
        final var doc = getIntermediateDocument(profile.getId(), slot);
        final var contents = testContentsFor(profile, "forced update " + slot);

        doc.setVersion("f00f");
        doc.setContents(contents);
        assertEquals(slot, doc.getSlot());

        final var result = getSaveDataDocumentDao().forceUpdateSaveDataDocument(doc);
        assertNotNull(result.getId());
        assertNotNull(result.getVersion());
        assertEquals(profile, result.getProfile());
        assertEquals(slot, result.getSlot());
        assertEquals(profile, result.getProfile());
        assertTrue(result.getTimestamp() >= now);

        putIntermediateDocument(profile.getId(), slot, result);

    }

    @Test(dependsOnMethods = {"testForceUpdateSaveDocumentUser", "testForceUpdateSaveDocumentProfile"})
    public void testGetAll() {

        final var first = getSaveDataDocumentDao().getSaveDataDocuments(
            0, 10,
            null, null);

        final var total = first.getTotal();
        assertEquals(40, first.getTotal());

        final var saves = new ArrayList<SaveDataDocument>();

        Pagination<SaveDataDocument> pagination;

        for (int i = 0; i < total; i += pagination.getObjects().size()) {
            pagination = getSaveDataDocumentDao().getSaveDataDocuments(i, 5, null, null);
            saves.addAll(pagination.getObjects());
        }

        assertEquals(40, saves.size());

        final var owners = Set.of(testUserBob, testUserAlice, testProfileBob, testProfileAlice);
        saves.forEach(sd -> assertTrue(owners.contains(sd.getUser()) || owners.contains(sd.getProfile())));

    }

    @Test(dataProvider = "getTestUsers", dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testFilterByUserId(final User user) {

        final var first = getSaveDataDocumentDao().getSaveDataDocuments(
            0, 10,
            user.getId(), null);

        final var total = first.getTotal();
        assertEquals(20, first.getTotal());

        final var saves = new ArrayList<SaveDataDocument>();

        Pagination<SaveDataDocument> pagination;

        for (int i = 0; i < total; i += pagination.getObjects().size()) {
            pagination = getSaveDataDocumentDao().getSaveDataDocuments(i, 5, user.getId(), null);
            saves.addAll(pagination.getObjects());
        }

        assertEquals(20, saves.size());

        saves.forEach(sgd -> assertEquals(user, sgd.getUser()));

        saves.stream()
             .filter(sgd -> sgd.getProfile() != null)
             .forEach(sgd -> assertEquals(user, sgd.getProfile().getUser()));

    }

    @Test(dataProvider = "getTestProfiles", dependsOnMethods = "testForceUpdateSaveDocumentProfile")
    public void testFilterByProfileId(final Profile profile) {

        final var first = getSaveDataDocumentDao().getSaveDataDocuments(
            0, 10,
            null, profile.getId());

        final var total = first.getTotal();
        assertEquals(10, first.getTotal());

        final var saves = new ArrayList<SaveDataDocument>();

        Pagination<SaveDataDocument> pagination;

        for (int i = 0; i < total; i += pagination.getObjects().size()) {
            pagination = getSaveDataDocumentDao().getSaveDataDocuments(i, 5, null, profile.getId());
            saves.addAll(pagination.getObjects());
        }

        assertEquals(10, saves.size());

        saves.forEach(sgd -> assertEquals(profile, sgd.getProfile()));
        saves.forEach(sgd -> assertEquals(profile.getUser(), sgd.getUser()));

    }

    @Test(dataProvider = "getTestProfiles", dependsOnMethods = "testForceUpdateSaveDocumentProfile")
    public void testFilterByUserAndProfileId(final Profile profile) {

        final var first = getSaveDataDocumentDao().getSaveDataDocuments(
            0, 10,
            profile.getUser().getId(), profile.getId());

        final var total = first.getTotal();
        assertEquals(10, first.getTotal());

        final var saves = new ArrayList<SaveDataDocument>();

        Pagination<SaveDataDocument> pagination;

        for (int i = 0; i < total; i += pagination.getObjects().size()) {

            pagination = getSaveDataDocumentDao().getSaveDataDocuments(
                i, 5,
                profile.getUser().getId(), profile.getId());

            saves.addAll(pagination.getObjects());

        }

        assertEquals(10, saves.size());

        saves.forEach(sgd -> assertEquals(profile, sgd.getProfile()));
        saves.forEach(sgd -> assertEquals(profile.getUser(), sgd.getUser()));

    }

    @Test(dependsOnMethods = {
        "testForceUpdateSaveDocumentUser",
        "testForceUpdateSaveDocumentProfile"
    })
    public void testFilterUnauthorized() {

        var total = getSaveDataDocumentDao().getSaveDataDocuments(
            0, 10,
            testUserTrudy.getId(), null).getTotal();

        assertEquals(0, total);

        total = getSaveDataDocumentDao().getSaveDataDocuments(
            0, 10,
            null, testProfileTrudy.getId()).getTotal();

        assertEquals(0, total);

        total = getSaveDataDocumentDao().getSaveDataDocuments(
            0, 10,
            testUserTrudy.getId(), testProfileTrudy.getId()).getTotal();

        assertEquals(0, total);

    }

    @Test(dataProvider = "getIntermediateSaveGameData", dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testGetById(final SaveDataDocument saveDataDocument) {
        getSaveDataDocumentDao().getSaveDataDocument(saveDataDocument.getId());
    }

    @Test(dataProvider = "getIntermediateSaveGameData", dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testFindById(final SaveDataDocument saveDataDocument) {
        final var doc = getSaveDataDocumentDao().findSaveDataDocument(saveDataDocument.getId());
        assertTrue(doc.isPresent());
    }

    @DataProvider
    public Object[][] getIntermediateUserSaveGameData() {
        return intermediateDocuments
            .values()
            .stream()
            .filter(sdd -> sdd.getProfile() == null)
            .map(sdd -> new Object[]{sdd})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getIntermediateUserSaveGameData", dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testGetByUserSlot(final SaveDataDocument saveDataDocument) {

        final var doc = getSaveDataDocumentDao().getUserSaveDataDocumentBySlot(
            saveDataDocument.getUser().getId(),
            saveDataDocument.getSlot()
        );

        assertEquals(saveDataDocument.getId(), doc.getId());

    }

    @Test(dataProvider = "getIntermediateUserSaveGameData", dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testFindByUserSlot(final SaveDataDocument saveDataDocument) {

        final var doc = getSaveDataDocumentDao().findUserSaveDataDocumentBySlot(
            saveDataDocument.getUser().getId(),
            saveDataDocument.getSlot()
        );

        assertTrue(doc.isPresent());
        assertEquals(saveDataDocument.getId(), doc.get().getId());

    }

    @DataProvider
    public Object[][] getIntermediateProfileSaveGameData() {
        return intermediateDocuments
            .values()
            .stream()
            .filter(sdd -> sdd.getProfile() != null)
            .map(sdd -> new Object[]{sdd})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getIntermediateProfileSaveGameData", dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testGetByProfileSlot(final SaveDataDocument saveDataDocument) {

        var doc = getSaveDataDocumentDao().getProfileSaveDataDocumentBySlot(
            saveDataDocument.getProfile().getId(),
            saveDataDocument.getSlot()
        );

        assertEquals(saveDataDocument.getId(), doc.getId());

    }

    @Test(dataProvider = "getIntermediateProfileSaveGameData", dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testFindByProfileSlot(final SaveDataDocument saveDataDocument) {

        final var doc = getSaveDataDocumentDao().findProfileSaveDataDocumentBySlot(
            saveDataDocument.getProfile().getId(),
            saveDataDocument.getSlot()
        );

        assertTrue(doc.isPresent());
        assertEquals(saveDataDocument.getId(), doc.get().getId());

    }


    @Test(dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testFindByIdAbsent() {
        final MongoSaveDataDocumentId bogus = new MongoSaveDataDocumentId(new ObjectId(), 0);
        final var doc = getSaveDataDocumentDao().findSaveDataDocument(bogus.toHexString());
        assertTrue(doc.isEmpty());
    }

    @Test(dependsOnMethods = "testForceUpdateSaveDocumentUser", expectedExceptions = SaveDataNotFoundException.class)
    public void testGetByIdAbsent() {
        final MongoSaveDataDocumentId bogus = new MongoSaveDataDocumentId(new ObjectId(), 0);
        getSaveDataDocumentDao().getSaveDataDocument(bogus.toHexString());
    }

    @Test(dependsOnMethods = "testForceUpdateSaveDocumentUser")
    public void testFindByIdBadId() {
        final var doc = getSaveDataDocumentDao().findSaveDataDocument("bogomcbogo");
        assertTrue(doc.isEmpty());
    }

    @Test(dependsOnMethods = "testForceUpdateSaveDocumentUser", expectedExceptions = SaveDataNotFoundException.class)
    public void testGetByIdBadId() {
        getSaveDataDocumentDao().getSaveDataDocument("bogomcbogo");
    }

    @AfterClass
    public void cleanupAllDocuments() {
        for (var doc : intermediateDocuments.values()) {
            getSaveDataDocumentDao().deleteSaveDocument(doc.getId());
            assertTrue(getSaveDataDocumentDao().findSaveDataDocument(doc.getId()).isEmpty());
        }

        final var pagination = getSaveDataDocumentDao()
            .getSaveDataDocuments(
                0, 100,
                null, null
            );

        assertEquals(0, pagination.getTotal());

    }

    private void putIntermediateDocument(final String id, final int slot, final SaveDataDocument document) {
        intermediateDocuments.put(new DocumentKey(id, slot), document);
    }

    private SaveDataDocument getIntermediateDocument(final String id, final int slot) {
        final var doc = intermediateDocuments.get(new DocumentKey(id, slot));
        return getMapper().map(doc, SaveDataDocument.class);
    }

    private String testContentsFor(final User user, final String message) {
        return format("Save Contents for %s - %s: %s", user.getName(), user.getId(), message);
    }

    private String testContentsFor(final Profile user, final String message) {
        return format("Save Contents for %s - %s: %s", user.getDisplayName(), user.getId(), message);
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(@Named(TEST_COMPONENT) Mapper mapper) {
        this.mapper = mapper;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public ProfileTestFactory getProfileTestFactory() {
        return profileTestFactory;
    }

    @Inject
    public void setProfileTestFactory(ProfileTestFactory profileTestFactory) {
        this.profileTestFactory = profileTestFactory;
    }

    public ApplicationTestFactory getApplicationTestFactory() {
        return applicationTestFactory;
    }

    @Inject
    public void setApplicationTestFactory(ApplicationTestFactory applicationTestFactory) {
        this.applicationTestFactory = applicationTestFactory;
    }

    public SaveDataDocumentDao getSaveDataDocumentDao() {
        return saveDataDocumentDao;
    }

    @Inject
    public void setSaveDataDocumentDao(SaveDataDocumentDao saveDataDocumentDao) {
        this.saveDataDocumentDao = saveDataDocumentDao;
    }

    private class DocumentKey {

        private final String key;

        private final int slot;

        public DocumentKey(final String key, final int slot) {
            this.key = key;
            this.slot = slot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DocumentKey that = (DocumentKey) o;
            return slot == that.slot && Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, slot);
        }

    }

}
