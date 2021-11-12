package com.namazustudios.socialengine;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.SimpleAttributes;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.namazustudios.socialengine.TestUtils.getUnixFSTest;
import static com.namazustudios.socialengine.TestUtils.getXodusTest;
import static java.util.UUID.randomUUID;

public class LuaAuthUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(LuaAuthUnitTest.class);

    private Context context;

    @Factory
    public Object[] getTests() {
        return new Object[] {
            getXodusTest(LuaAuthUnitTest.class),
            getUnixFSTest(LuaAuthUnitTest.class)
        };
    }

    @Test
    public void testProfile() throws Exception {

        final Profile profile = mockProfile();
        final Attributes attributes = new SimpleAttributes.Builder()
            .setAttribute(Profile.PROFILE_ATTRIBUTE, profile)
            .build();

        performLuaTest("namazu.elements.test.auth", "test_profile", attributes);

    }

    @Test
    public void testProfileRemote() throws Exception {

        final Profile profile = mockProfile();
        final Attributes attributes = new SimpleAttributes.Builder()
            .setAttribute(Profile.PROFILE_ATTRIBUTE, profile)
            .build();

        performLuaTest("namazu.elements.test.auth", "test_profile_remote", attributes);

    }


    @Test
    public void testProfileUnknown() throws Exception {
        final Attributes attributes = Attributes.emptyAttributes();
        performLuaTest("namazu.elements.test.auth", "test_profile_unknown", attributes);
    }

    @Test
    public void testAuthenticatedUser() throws Exception {

        final User user = mockUser();
        final Attributes attributes = new SimpleAttributes.Builder()
            .setAttribute(User.USER_ATTRIBUTE, user)
            .build();

        performLuaTest("namazu.elements.test.auth", "test_authenticated_user", attributes);

    }

    @Test
    public void testAuthenticatedUserRemote() throws Exception {

        final User user = mockUser();
        final Attributes attributes = new SimpleAttributes.Builder()
                .setAttribute(User.USER_ATTRIBUTE, user)
                .build();

        performLuaTest("namazu.elements.test.auth", "test_authenticated_user_remote", attributes);

    }

    @Test
    public void testUnauthenticatedUser() throws Exception {
        final Attributes attributes = Attributes.emptyAttributes();
        performLuaTest("namazu.elements.test.auth", "test_unauthenticated_user", attributes);
    }

    @Test
    public void testAuthInventory() throws Exception {

        final User user = mockUser();

        final Attributes attributes = new SimpleAttributes.Builder()
                .setAttribute(User.USER_ATTRIBUTE, user)
                .build();

        performLuaTest("namazu.elements.test.auth", "test_auth_inventory", attributes);

    }

    @Test
    public void testAuthInventoryRemote() throws Exception {

        final User user = mockUser();

        final Attributes attributes = new SimpleAttributes.Builder()
                .setAttribute(User.USER_ATTRIBUTE, user)
                .build();

        performLuaTest("namazu.elements.test.auth", "test_auth_inventory_remote", attributes);

    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

    public static Profile mockProfile() {
        final Profile profile = new Profile();
        profile.setDisplayName("Example McExampleson");
        profile.setImageUrl("http://example.com/profile.png");
        profile.setUser(mockUser());
        profile.setApplication(mockApplication());
        profile.setId("ExampleProfileId");
        return profile;
    }

    public static User mockUser() {
        final User user = new User();
        user.setLevel(User.Level.USER);
        user.setEmail("example@example.com");
        user.setActive(true);
        user.setName("example");
        user.setId("ExampleUserId");
        return user;
    }

    public void performLuaTest(final String moduleName,
                               final String methodName,
                               final Attributes attributes) throws InterruptedException {
        final Path path = new Path("socialengine-auth-test-" + randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().createAttributes(moduleName, path, attributes);
        final Object result = getContext().getResourceContext().invoke(resourceId, methodName);
        logger.info("Successfuly got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);
    }

    public static Application mockApplication() {
        final Application application = new Application();
        application.setName("Example");
        application.setDescription("Example Application");
        application.setId("ExampleApplicationId");
        return application;
    }

}
