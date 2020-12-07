package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.follower.Follower;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoFollowerDaoTest {

    private FollowerDao followerDao;

    private ProfileDao profileDao;

    private UserDao userDao;

    private ApplicationDao applicationDao;

    private Application testApplication;

    private User testUserA;

    private User testUserB;

    private Profile testProfileA;

    private Profile testProfileB;

    @BeforeClass
    public void setupTestItems() {
        makeTestApplication();
        makeTestUsers();
        makeTestProfiles();

        final Follower follower = new Follower();
        follower.setProfileId(testProfileA.getId());
        follower.setFollowedId(testProfileB.getId());
        getFollowerDao().createFollowerForProfile(follower);
    }

    @Test(groups = "first")
    public void testGetFollowers() {
        Pagination<Profile> followedProfiles = getFollowerDao().getFollowersForProfile(testProfileA.getId(), 0, 20);
        assertEquals(followedProfiles.iterator().next(), testProfileB);
    }

    @Test(groups = "first")
    public void testGetFollower() {
        Profile followedProfile = getFollowerDao().getFollowerForProfile(testProfileA.getId(), testProfileB.getId());
        assertEquals(followedProfile, testProfileB);
    }

    @Test(dependsOnGroups = "first", expectedExceptions = NotFoundException.class)
    public void testGetFollowerFails() {
        getFollowerDao().getFollowerForProfile(testProfileB.getId(), testProfileA.getId());
    }

    @Test(dependsOnGroups = "first")
    public void testDeleteFollower(){
        getFollowerDao().deleteFollowerForProfile(testProfileA.getId(), testProfileB.getId());
    }

    @Test(dependsOnGroups = "first", expectedExceptions = NotFoundException.class)
    public void testDeleteFollowerFails(){
        getFollowerDao().deleteFollowerForProfile(testProfileB.getId(), testProfileA.getId());
    }

    private void makeTestApplication() {
        final Application application = new Application();
        application.setName("mock");
        application.setDescription("A mock application.");
        testApplication = getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    private void makeTestUsers(){
        testUserA = new User();
        testUserA.setName("testy.mctesterson.2");
        testUserA.setEmail("testy.mctesterson.2@example.com");
        testUserA.setLevel(USER);

        testUserB = new User();
        testUserB.setName("testy.mctesterson.3");
        testUserB.setEmail("testy.mctesterson.3@example.com");
        testUserB.setLevel(USER);

        testUserA = getUserDao().createOrReactivateUser(testUserA);
        testUserB = getUserDao().createOrReactivateUser(testUserB);
    }

    private void makeTestProfiles(){
        testProfileA = new Profile();
        testProfileA.setDisplayName("testyA");
        testProfileA.setImageUrl("testyA/image.png");
        testProfileA.setUser(testUserA);
        testProfileA.setApplication(testApplication);

        testProfileB = new Profile();
        testProfileB.setDisplayName("testyB");
        testProfileB.setImageUrl("testyB/image.png");
        testProfileB.setUser(testUserB);
        testProfileB.setApplication(testApplication);

        testProfileA = getProfileDao().createOrReactivateProfile(testProfileA);
        testProfileB = getProfileDao().createOrReactivateProfile(testProfileB);
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

    public FollowerDao getFollowerDao() {
        return followerDao;
    }

    @Inject
    public void setFollowerDao(FollowerDao followerDao) {
        this.followerDao = followerDao;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

}
