package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoRefreshProfileDaoTest {

    private ProfileDao profileDao;

    private List<Profile> profiles = new CopyOnWriteArrayList<>();

    private MongoProfileTestOperations mongoProfileTestOperations;

    @BeforeClass
    public void createUsersAndApplications() {
        getMongoProfileTestCore().createUsersAndApplications(MongoRefreshProfileDaoTest.class, 5, 5);
    }

    @DataProvider
    public Object[][] profiles() {
        return profiles
                .stream()
                .map(p -> new Object[]{p})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] applicationsAndUsers() {
        return getMongoProfileTestCore().applicationsAndUsers();
    }

    @Test(dataProvider = "applicationsAndUsers", groups = "create")
    public void testCreate(final Application application, final User user) {

        final var profile = new Profile();
        profile.setUser(user);
        profile.setApplication(application);
        profile.setDisplayName("TestUser");
        profile.setImageUrl("https://example.com/image.png");

        final var refreshed = getProfileDao().createOrRefreshProfile(profile);
        assertNotNull(refreshed.getId());
        assertEquals(refreshed.getUser().getId(), user.getId());
        assertEquals(refreshed.getApplication().getId(), application.getId());
        assertEquals(refreshed.getDisplayName(), profile.getDisplayName());
        assertEquals(refreshed.getImageUrl(), profile.getImageUrl());

        profiles.add(refreshed);

    }

    @Test(dataProvider = "profiles", groups = "update", dependsOnGroups = "create")
    public void testRefresh(final Profile original) {

        final var profile = new Profile();
        profile.setUser(original.getUser());
        profile.setApplication(original.getApplication());
        profile.setDisplayName("TestUserAgain");
        profile.setImageUrl("https://example.com/image-2.png");

        final var refreshed = getProfileDao().createOrRefreshProfile(profile);
        assertNotNull(refreshed.getId());
        assertEquals(refreshed.getUser().getId(), original.getUser().getId());
        assertEquals(refreshed.getApplication().getId(), original.getApplication().getId());
        assertEquals(refreshed.getDisplayName(), original.getDisplayName());
        assertEquals(refreshed.getImageUrl(), original.getImageUrl());

    }


    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public MongoProfileTestOperations getMongoProfileTestCore() {
        return mongoProfileTestOperations;
    }

    @Inject
    public void setMongoProfileTestCore(MongoProfileTestOperations mongoProfileTestOperations) {
        this.mongoProfileTestOperations = mongoProfileTestOperations;
    }

}
