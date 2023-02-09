package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.namazustudios.socialengine.dao.*;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.profile.ProfileNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.follower.CreateFollowerRequest;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import org.bson.types.ObjectId;
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

    private UserTestFactory userTestFactory;

    @BeforeClass
    public void setupTestItems() {
        makeTestApplication();
        makeTestUsers();
        makeTestProfiles();
    }

    @Test()
    public void createFollowerForProfile(){
        final CreateFollowerRequest createFollowerRequest = new CreateFollowerRequest();
        createFollowerRequest.setFollowedId(testProfileB.getId());
        getFollowerDao().createFollowerForProfile(testProfileA.getId(), createFollowerRequest);
    }

    @Test(dependsOnMethods = "createFollowerForProfile", expectedExceptions = DuplicateException.class)
    public void createFollowerForProfileDuplicate(){
        final CreateFollowerRequest createFollowerRequest = new CreateFollowerRequest();
        createFollowerRequest.setFollowedId(testProfileB.getId());
        getFollowerDao().createFollowerForProfile(testProfileA.getId(), createFollowerRequest);
    }

    @Test(dependsOnMethods = "createFollowerForProfile", expectedExceptions = ProfileNotFoundException.class)
    public void createFollowerForProfileFails(){
        final CreateFollowerRequest createFollowerRequest = new CreateFollowerRequest();
        createFollowerRequest.setFollowedId(new ObjectId().toString());
        getFollowerDao().createFollowerForProfile(new ObjectId().toString(), createFollowerRequest);
    }

    @Test(dependsOnMethods = "createFollowerForProfile")
    public void testGetFollowers() {
        Pagination<Profile> followedProfiles = getFollowerDao().getFollowersForProfile(testProfileA.getId(), 0, 20);
        assertEquals(followedProfiles.iterator().next(), testProfileB);
    }

    @Test(dependsOnMethods = "createFollowerForProfile")
    public void testGetFollower() {
        Profile followedProfile = getFollowerDao().getFollowerForProfile(testProfileA.getId(), testProfileB.getId());
        assertEquals(followedProfile, testProfileB);
    }

    @Test(dependsOnMethods = "createFollowerForProfile", expectedExceptions = NotFoundException.class)
    public void testGetFollowerFails() {
        getFollowerDao().getFollowerForProfile(testProfileB.getId(), testProfileA.getId());
    }

    @Test(dependsOnMethods = {"testGetFollower", "testGetFollowers"})
    public void testDeleteFollower(){
        getFollowerDao().deleteFollowerForProfile(testProfileA.getId(), testProfileB.getId());
    }

    @Test(dependsOnMethods = {"testGetFollower", "testGetFollowers"}, expectedExceptions = NotFoundException.class)
    public void testDeleteFollowerFails(){
        getFollowerDao().deleteFollowerForProfile(testProfileB.getId(), testProfileA.getId());
    }

    private void makeTestApplication() {
        final Application application = new Application();
        application.setName("mock_follower");
        application.setDescription("A mock application for testing followers.");
        testApplication = getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    private void makeTestUsers(){
        testUserA = getUserTestFactory().createTestUser();
        testUserB = getUserTestFactory().createTestUser();
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

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
