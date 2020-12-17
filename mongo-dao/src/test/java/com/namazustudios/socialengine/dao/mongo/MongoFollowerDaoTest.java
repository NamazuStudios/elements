package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.namazustudios.socialengine.dao.*;
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

    @Test(dependsOnMethods = "createFollowerForProfile", expectedExceptions = DuplicateKeyException.class)
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
        final CreateProfileRequest createProfileRequest = new CreateProfileRequest();
        createProfileRequest.setDisplayName("testyA");
        createProfileRequest.setImageUrl("testyA/image.png");
        createProfileRequest.setUserId(testUserA.getId());
        createProfileRequest.setApplicationId(testApplication.getId());

        final CreateProfileRequest createProfileRequest1 = new CreateProfileRequest();
        createProfileRequest1.setDisplayName("testyB");
        createProfileRequest1.setImageUrl("testyB/image.png");
        createProfileRequest1.setUserId(testUserB.getId());
        createProfileRequest1.setApplicationId(testApplication.getId());

        testProfileA = getProfileDao().createOrReactivateProfile(createProfileRequest);
        testProfileB = getProfileDao().createOrReactivateProfile(createProfileRequest1);
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
